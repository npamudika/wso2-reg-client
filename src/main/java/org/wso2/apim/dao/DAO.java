package org.wso2.apim.dao;

import org.wso2.apim.dto.RegistryContent;
import org.wso2.apim.dto.RegistryPath;
import org.wso2.apim.dto.RegistryResource;
import org.wso2.apim.exception.DAOException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Database Access Object used for the registry client
 */
public class DAO {
    HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
    private APIMPostgresDataSource dataSource;
    private List<RegistryResource> registryResourcesList = new ArrayList<RegistryResource>();
    private List<RegistryPath> registryPathsList = new ArrayList<RegistryPath>();
    private List<String> apiNames = new ArrayList<>();
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

    public List<RegistryPath> getRegistryPaths() throws DAOException {
        int count = 0;
        final String getRegistryPathFromRegPathIdSql = "SELECT reg_path_id, reg_path_value, reg_path_parent_id, reg_tenant_id "
                + "FROM reg_path WHERE reg_path_id =?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getRegistryPathFromRegPathIdSql)) {
            for (int i = 0; i < registryResourcesList.size(); i++) {
                preparedStatement.setInt(1, registryResourcesList.get(i).getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                RegistryPath registryPath = null;
                while (resultSet.next()) {
                    registryPath = new RegistryPath();
                    registryPath.setId(resultSet.getInt("reg_path_id"));
                    registryPath.setRegPathValue(resultSet.getString("reg_path_value"));

                    //52 is the length of "/_system/governance/apimgt/applicationdata/provider/"
                    String customPath = registryPath.getRegPathValue().substring(52);
                    String[] paths = customPath.split("/");

                    String providerName = paths[0];
                    String apiName = paths[1];
                    registryPath.setApiName(apiName);
                    registryPath.setProvider(providerName);
                    System.out.println("Provider Name**** = " + providerName);
                    System.out.println("apiName = " + apiName);
                    registryPathsList.add(registryPath);
                    System.out.println(registryPath.toString());
                    count++;
                }
            }
            System.out.println("There are " + count + " entries for reg_path which have NULL for reg_content_id.");
            return registryPathsList;
        } catch (SQLException e) {
            throw new DAOException("Error while getting Registry Resource.");
        }
    }

    /**
     * @param registryPath This method will iterate through the RegContent table and decrypt all the entries, Then findout the entry which is mapping to the given registry path. As there are multiple entries, It will find the largest contentID entry for the given resource. Then at last it will prepare the update SQL and update the table
     */
    public void findAndUpdateContentIDMissingEntries(RegistryPath registryPath) throws DAOException, IOException, ParserConfigurationException, TransformerException, SAXException, SQLException {
        final String getAPIContentFromRegContentIdSql = "SELECT reg_content_id, reg_content_data, reg_tenant_id " +
                "FROM reg_content";
        HashMap<String, Integer> regResouces = new HashMap();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(getAPIContentFromRegContentIdSql);
            ResultSet resultSet = preparedStatement.executeQuery();
            regResouces.put(registryPath.getApiName(), 0);
            RegistryContent registryContent = null;
            while (resultSet.next()) {
                registryContent = new RegistryContent();
                //Convert Blob to String
                String query = null;
                query = IOUtils.toString(resultSet.getBinaryStream("reg_content_data"));

                //Filtering out regContent with related to API name, Provider
                if (query.contains("<metadata xmlns=\"http://www.wso2.org/governance/metadata\">") && (query.contains("<provider>" + registryPath.getProvider() + "</provider>") || query.contains("<provider>" + registryPath.getProvider() + "/" + registryPath.getApiName() + "</provider>")) && query.contains(registryPath.getApiName()) && query.contains("<name>") && query.contains("<version>")) {
                    //Iterate until get the largest content ID , as it is an auto increment value
                    if (resultSet.getInt("reg_content_id") > regResouces.get(registryPath.getApiName())) {
                        regResouces.put(registryPath.getApiName(), resultSet.getInt("reg_content_id"));
                    }
                }
            }
            System.out.println("API Name  " + registryPath.getApiName() + "Reg Path Id " + registryPath.getId() + "*********Latest ContentId******** = " + regResouces.get(registryPath.getApiName()));

            //Updating the tables
            String updateSQL = "UPDATE reg_resource  SET reg_content_id = " + regResouces.get(registryPath.getApiName()) + " WHERE reg_path_id = " + registryPath.getId() + " and reg_name = 'api';";

            System.out.println("updateSQL = " + updateSQL);
            PreparedStatement preparedStatement2 = connection.prepareStatement(updateSQL);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error while getting Registry Content.", e);
        }
    }
}
