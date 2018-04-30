package org.wso2.apim.dao;

import org.wso2.apim.exception.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database Access Object used for the registry client
 */
public class DAO {
    private APIMPostgresDataSource dataSource;

    public DAO(String dataSourcePath) {
        //Initializes a hickariCP[https://github.com/brettwooldridge/HikariCP] datasource
        // from the provided data source properties file
        dataSource = new APIMPostgresDataSource(dataSourcePath);
    }

    /**
     * Retrieves an application object from the DB corresponding to the given application owner and application name
     *
     * @return Api object
     * @throws DAOException if error occurred during accessing the DB
     */
    public void getApi() throws DAOException {
        final String getAPIFromRegPathIdSql = "SELECT reg_path_id, reg_name, reg_media_type, reg_creator, " +
                "       reg_created_time, reg_last_updator, reg_last_updated_time," +
                "       reg_content_id, reg_tenant_id, reg_uuid" +
                "  FROM reg_resource where reg_path_id = 577;";
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(getAPIFromRegPathIdSql);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println(resultSet);
        } catch (SQLException e) {
            throw new DAOException("Error while getting API.");
        }
    }
}
