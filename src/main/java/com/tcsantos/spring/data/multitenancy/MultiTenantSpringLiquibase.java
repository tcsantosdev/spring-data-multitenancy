package com.tcsantos.spring.data.multitenancy;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class MultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {

	private Logger log = LogFactory.getInstance().getLog(MultiTenantSpringLiquibase.class.getName());

	private DataSource dataSource;

	private Collection<Tenant> tenants;

	private ResourceLoader resourceLoader;

	private String labels;

	private Map<String, String> parameters;

	private String defaultSchema;

	private boolean dropFirst = false;

	private File rollbackFile;

	private MultitenancyLiquibaseProperties multitenancyLiquibaseProperties;
	
	public MultiTenantSpringLiquibase(MultitenancyLiquibaseProperties multitenancyLiquibaseProperties) {
		this.multitenancyLiquibaseProperties = multitenancyLiquibaseProperties;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Tenant based multitenancy enabled");
		if (tenants == null || tenants.isEmpty()) {
			log.warning("No tenant defined, migration aborted.");
		} else {
			runOnAllTenants();
		}
	}

	private void runOnAllTenants() throws LiquibaseException {
		for (Tenant tenant : tenants) {
			String identifier = tenant.getIdentifier();
			String schemaName = tenant.getSchemaName();

			log.info(String.format("Initializing Liquibase for tenant (%s), schema (%s).", identifier, schemaName));
			SpringLiquibase liquibase = getSpringLiquibase(dataSource, identifier);
			liquibase.setDefaultSchema(schemaName);
			
			log.info(String.format("Contexts (%s).", liquibase.getContexts()));
			
			liquibase.afterPropertiesSet();
			log.info(String.format("Liquibase ran for tenant (%s).", identifier));
		}
	}

	private CustomSpringLiquibase getSpringLiquibase(DataSource dataSource, String tenantIdentifier) {
		String tenantContext = getTenantContext(tenantIdentifier);

		CustomSpringLiquibase liquibase = new CustomSpringLiquibase();
		liquibase.setChangeLog(multitenancyLiquibaseProperties.getChangelogPath());
		liquibase.setChangeLogParameters(parameters);
		liquibase.setContexts(tenantContext);
		liquibase.setLabels(labels);
		liquibase.setDropFirst(dropFirst);
		liquibase.setShouldRun(multitenancyLiquibaseProperties.isEnabled());
		liquibase.setRollbackFile(rollbackFile);
		liquibase.setResourceLoader(resourceLoader);
		liquibase.setDataSource(dataSource);
		liquibase.setDefaultSchema(defaultSchema);
		return liquibase;
	}
	
	private String getTenantContext(String tenantIdentifier) {
		String tenantContexts = multitenancyLiquibaseProperties.getTenantContexts().get(tenantIdentifier);
		
		StringJoiner joiner = new StringJoiner(",");
		
		if(!StringUtils.isEmpty(multitenancyLiquibaseProperties.getContexts())) {
			joiner.add(multitenancyLiquibaseProperties.getContexts());
		}
		
		if(!StringUtils.isEmpty(tenantContexts)) {
			joiner.add(tenantContexts);
		}
		
		return joiner.toString();
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	public boolean isDropFirst() {
		return dropFirst;
	}

	public void setDropFirst(boolean dropFirst) {
		this.dropFirst = dropFirst;
	}

	public File getRollbackFile() {
		return rollbackFile;
	}

	public void setRollbackFile(File rollbackFile) {
		this.rollbackFile = rollbackFile;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Collection<Tenant> getTenants() {
		return tenants;
	}

	public void setTenants(Collection<Tenant> tenants) {
		this.tenants = tenants;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
