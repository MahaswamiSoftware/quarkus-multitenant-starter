package com.mahaswami.quarkus.multitenant.provider;

import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

@SuppressWarnings("serial")
public class SchemaMultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider {

    private ConnectionProvider connectionProvider;

    public SchemaMultiTenantConnectionProvider() throws IOException {
        connectionProvider = initConnectionProvider();
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return connectionProvider;
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return connectionProvider;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {

        System.out.println("@@@@@@@@ " + new Date() + " current tenant identifier: " + tenantIdentifier);

        Connection connection = super.getConnection(tenantIdentifier);
        try {
            connection.createStatement().execute("SET search_path to " + tenantIdentifier);
        } catch (Exception e) {
            System.out.println("get connection exception: " + e.getMessage());
            this.releaseConnection(tenantIdentifier, connection);
            e.printStackTrace();
            throw new SQLException("Could not alter JDBC connection to schema ["+ tenantIdentifier +"]");
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        super.releaseConnection(tenantIdentifier, connection);
    }

    private ConnectionProvider initConnectionProvider() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/hibernate-schema-multitenancy.properties"));

        DriverManagerConnectionProviderImpl connectionProvider = new DriverManagerConnectionProviderImpl();
        connectionProvider.configure(properties);
        return connectionProvider;
    }
}
