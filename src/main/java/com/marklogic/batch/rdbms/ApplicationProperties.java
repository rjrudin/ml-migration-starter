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

    //MarkLogic ConnectionProperties
    private List<String> markLogicHosts;
    private int markLogicPort;
    private String markLogicUsername;
    private String markLogicPassword;

    @Autowired
    public void setHosts(
            @Value("#{'${marklogic.hosts}'.split(',')}") List<String> hosts) {
        this.markLogicHosts = hosts;
    }

    @Autowired
    public void setPort(
            @Value("${marklogic.port}") int port) {
        this.markLogicPort = port;
    }

    public List<String> getMarkLogicHosts() {
        return markLogicHosts;
    }

    public int getMarkLogicPort() {
        return markLogicPort;
    }

    public String getMarkLogicUsername() {
        return markLogicUsername;
    }

    @Autowired
    public void setMarkLogicUsername(
            @Value("${marklogic.username}") String markLogicUsername) {
        this.markLogicUsername = markLogicUsername;
    }

    public String getMarkLogicPassword() {
        return markLogicPassword;
    }

    @Autowired
    public void setMarkLogicPassword(
            @Value("${marklogic.password}") String markLogicPassword) {
        this.markLogicPassword = markLogicPassword;
    }

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

    protected DatabaseClient getDatabaseClient() {
        return DatabaseClientFactory.newClient(
                getMarkLogicHosts().get(0),
                getMarkLogicPort(),
                new DatabaseClientFactory.DigestAuthContext(getMarkLogicUsername(), getMarkLogicPassword())
        );
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
