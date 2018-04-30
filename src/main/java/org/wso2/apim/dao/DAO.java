package org.wso2.apim.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.apim.dto.RegistryContent;
import org.wso2.apim.dto.RegistryPath;
import org.wso2.apim.dto.RegistryResource;
import org.wso2.apim.exception.DAOException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Access Object used for the registry client
 */
public class DAO {
    private APIMPostgresDataSource dataSource;
    private List<RegistryResource> registryResourcesList = new ArrayList<RegistryResource>();
    private List<RegistryPath> registryPathsList = new ArrayList<RegistryPath>();
    private List<RegistryContent> registryContentIdsList = new ArrayList<RegistryContent>();

    public DAO(String dataSourcePath) {
        //Initializes a hickariCP[https://github.com/brettwooldridge/HikariCP] datasource
        // from the provided data source properties file
        dataSource = new APIMPostgresDataSource(dataSourcePath);
    }

    /**
     * Retrieves a list of registry resource objects from the DB where reg_content_id is NULL
     *
     * @return List of Registry Resource objects
     * @throws DAOException if error occurred during accessing the DB
     */
    public void getRegistryResources() throws DAOException {
        int count = 0;
        final String getAPIFromRegPathIdSql = "SELECT reg_path_id, reg_name, reg_version, reg_media_type, reg_creator, "
        + "reg_created_time, reg_last_updator, reg_last_updated_time, reg_description, "
        + "reg_content_id, reg_uuid "
        + "FROM reg_resource WHERE reg_name = 'api' and reg_media_type = 'application/vnd.wso2-api+xml' and reg_content_id IS NULL order by reg_last_updated_time;";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(getAPIFromRegPathIdSql);
            ResultSet resultSet = preparedStatement.executeQuery();
            RegistryResource registryResource = null;
            while (resultSet.next()) {
                registryResource = new RegistryResource();
                registryResource.setId(resultSet.getInt("reg_path_id"));
                registryResource.setRegName(resultSet.getString("reg_name"));
                registryResource.setRegContentId(resultSet.getInt("reg_content_id"));
                registryResourcesList.add(registryResource);
                System.out.println(registryResource.toString());
                count++;
            }
            System.out.println("There are " + count + " entries which have NULL for reg_content_id.");
        } catch (SQLException e) {
            throw new DAOException("Error while getting Registry Resource.");
        }
    }

    public void getRegistryPaths() throws DAOException {
        int count = 0;
        final String getRegistryPathFromRegPathIdSql = "SELECT reg_path_id, reg_path_value, reg_path_parent_id, reg_tenant_id "
                + "FROM reg_path WHERE reg_path_id =?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getRegistryPathFromRegPathIdSql)) {
            for (int i = 0 ; i< registryResourcesList.size() ; i++) {
                preparedStatement.setInt(1, registryResourcesList.get(i).getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                RegistryPath registryPath = null;
                while (resultSet.next()) {
                    registryPath = new RegistryPath();
                    registryPath.setId(resultSet.getInt("reg_path_id"));
                    registryPath.setRegPathValue(resultSet.getString("reg_path_value"));
                    registryPathsList.add(registryPath);
                    System.out.println(registryPath.toString());
                    count++;
                }
            }
            System.out.println("There are " + count + " entries for reg_path which have NULL for reg_content_id.");
        } catch (SQLException e) {
            throw new DAOException("Error while getting Registry Resource.");
        }
    }

    /**
     * Retrieves registry content objects from the DB
     *
     * @return Registry Content object
     * @throws DAOException if error occurred during accessing the DB
     */
    public void getRegContent() throws DAOException, IOException, ParserConfigurationException, TransformerException, SAXException {
        final String getAPIContentFromRegContentIdSql = "SELECT reg_content_id, reg_content_data, reg_tenant_id " +
                "FROM reg_content;";
        int count = 0;
        String regPath = null;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(getAPIContentFromRegContentIdSql);
            ResultSet resultSet = preparedStatement.executeQuery();
            RegistryContent registryContent = null;
            while (resultSet.next()) {
                registryContent = new RegistryContent();
                //Convert Blob to String
                String query = null;
                query = IOUtils.toString(resultSet.getBinaryStream("reg_content_data"));
                if (query.contains("<metadata xmlns=\"http://www.wso2.org/governance/metadata\">") && query.contains("<provider>")&& query.contains("<name>") && query.contains("<version>")) {
                    //Convert String to XML
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(query)));
                    document.getDocumentElement().normalize();
                    NodeList nodes = document.getElementsByTagName("overview");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node nNode = nodes.item(i);
                        Element eElement = (Element) nNode;
                        Element provider = (Element) eElement.getElementsByTagName("provider").item(0);
                        Element name = (Element) eElement.getElementsByTagName("name").item(0);
                        Element version = (Element) eElement.getElementsByTagName("version").item(0);
                        regPath = "/_system/governance/apimgt/applicationdata/provider/" + provider.getTextContent() + "/" + name.getTextContent() + "/" + version.getTextContent();
                        System.out.println(regPath);
                    }
                }
                for (int i = 0 ; i < registryPathsList.size() ; i++) {
                    if (regPath != null) {
                        if(registryPathsList.get(i).getRegPathValue().equals(regPath)) {
                            registryContent.setRegContentId(resultSet.getInt("reg_content_id"));
                            registryContent.setRegPath(regPath);
                            System.out.println(registryContent.toString());
                            registryContentIdsList.add(registryContent);
                            count++;
                        }
                    }
                }
            }
            System.out.println(count);
            System.out.println("Successfully executed query to find missing content Ids for the reg_paths.");
        } catch (SQLException e) {
            throw new DAOException("Error while getting Registry Content.");
        }
    }
}
