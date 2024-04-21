package com.github.lucky44x.luckybounties.SQL;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Lucky44x
 * The HikariConnectionPool for use with SQL based bounties and the SQL-Bountyhandler
 */
public class HikariConnectionPool {
    private final LuckyBounties instance;

    private HikariDataSource dataSource;

    public HikariConnectionPool(LuckyBounties instance){
        this.instance = instance;
    }

    /**
     * Opens a new SQL-Pool
     * @param url the url for the database
     * @param userName the username for the database login
     * @param password the password for the database login
     * @param driverName the driverName for determining which database-driver to use
     */
    public final void openPool(String url, String userName, String password, String driverName){
        dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(20);

        if(driverName != null)
            dataSource.setDriverClassName(driverName);

        dataSource.setJdbcUrl(url);

        dataSource.setUsername(userName);
        dataSource.setPassword(password);

        dataSource.addDataSourceProperty("createDatabaseIfNotExist", true);
    }

    /**
     * Closes the pool
     */
    public final void closePool(){
        dataSource.close();
    }

    /**
     * Gets the current connection to send a query
     * @return a connection to send queries
     */
    public Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
