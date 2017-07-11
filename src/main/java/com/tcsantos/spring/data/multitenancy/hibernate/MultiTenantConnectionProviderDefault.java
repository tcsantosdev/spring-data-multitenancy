package com.tcsantos.spring.data.multitenancy.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import com.tcsantos.spring.data.multitenancy.TenantResolver;

public class MultiTenantConnectionProviderDefault implements MultiTenantConnectionProvider {

	private static final long serialVersionUID = 1L;

	private DataSource dataSource;
	
	private TenantResolver tenantResolver;
	
	public MultiTenantConnectionProviderDefault(DataSource dataSource, TenantResolver tenantResolver) {
		this.dataSource = dataSource;
		this.tenantResolver = tenantResolver;
	}
	
	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();

		String schemaName = tenantResolver.resolveTenantSchemaName(tenantIdentifier);
		
		connection.setSchema(schemaName);
		
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		String schemaName = tenantResolver.resolveTenantSchemaName(tenantIdentifier);
		
		connection.setSchema(schemaName);

		connection.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		return null;
	}

}
