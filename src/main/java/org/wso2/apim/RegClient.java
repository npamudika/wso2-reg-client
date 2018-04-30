package org.wso2.apim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.apim.dao.DAO;
import org.wso2.apim.exception.DAOException;

/**
 * Registry Client Implementation.
 */
public class RegClient {
    private static final String SYS_PROP_ENV_CONF = "dbEnvConfig";
    private static final String SYS_PROP_ENV_CONF_DEFAULT = "datasources/db-env.properties";

    private static final Logger log = LoggerFactory.getLogger(RegClient.class);

    public static void main(String[] args) throws DAOException {
        //Setting environment details
        // by retrieving environment configs via system properties or using default config file
        String dbEnvProperties = System.getProperty(SYS_PROP_ENV_CONF) != null ?
                System.getProperty(SYS_PROP_ENV_CONF) : SYS_PROP_ENV_CONF_DEFAULT;

        //Create data access objects for the DB environment
        DAO dbEnvDAO = new DAO(dbEnvProperties);
        dbEnvDAO.getApi();
    }
}
