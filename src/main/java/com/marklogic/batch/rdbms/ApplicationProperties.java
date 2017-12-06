package com.marklogic.batch.rdbms;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
public class ApplicationProperties {

    //JDBC Database Connection Properties
    private String jdbcUrl;
    private String jdbcDriverName;
    private String jdbcUsername;
    private String jdbcPassword;


    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Autowired
    public void setJdbcUrl(
            @Value("${jdbc.url}") String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcDriverName() {
        return jdbcDriverName;
    }

    @Autowired
    public void setJdbcDriverName(
            @Value("${jdbc.driverName}") String jdbcDriverName) {
        this.jdbcDriverName = jdbcDriverName;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    @Autowired
    public void setJdbcUsername(
            @Value("${jdbc.username}") String username) {
        this.jdbcUsername = username;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    @Autowired
    public void setJdbcPassword(
            @Value("${jdbc.password}") String password) {
        this.jdbcPassword = password;
    }

    protected DataSource getDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(jdbcDriverName);
        ds.setUrl(jdbcUrl);
        ds.setUsername(jdbcUsername);
        ds.setPassword(jdbcPassword);
        return ds;
    }

}
