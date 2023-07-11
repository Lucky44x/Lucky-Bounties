package com.github.lucky44x.luckybounties.SQL;

import com.github.lucky44x.luckybounties.LuckyBounties;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionPool {
    private final LuckyBounties instance;

    private HikariDataSource dataSource;

    public HikariConnectionPool(LuckyBounties instance){
        this.instance = instance;
    }

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

    public final void closePool(){
        dataSource.close();
    }

    public Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
