package org.wso2.apim.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class APIMPostgresDataSource {
    private HikariDataSource dataSource;

    public APIMPostgresDataSource(String dataSourcePath) {
        HikariConfig config = new HikariConfig(dataSourcePath);
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
