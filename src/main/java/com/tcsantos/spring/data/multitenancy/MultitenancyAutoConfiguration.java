package com.tcsantos.spring.data.multitenancy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.tcsantos.spring.data.multitenancy.hibernate.CurrentTenantIdentifierResolverDefault;
import com.tcsantos.spring.data.multitenancy.hibernate.MultiTenantConnectionProviderDefault;

import liquibase.integration.spring.MultiTenantSpringLiquibase;

@Configuration
@PropertySource("classpath:/multitenancy-default.properties")
public class MultitenancyAutoConfiguration {

	@Value("${multitenancy.entity.package.scan}")
	private String packageToScan;

	@Value("${multitenancy.liquibase.contexts:#{null}}")
	private String multiTenancyLiquibaseContexts;

	@Value("${multitenancy.liquibase.enabled:true}")
	private boolean multiTenancyLiquibaseEnabled;

	@Value("${multitenancy.liquibase.changelog.path:classpath:db/changelog/db.changelog-tenant-master.xml}")
	private String changeLogPath;

	@Bean
	@ConditionalOnMissingBean
	public CurrentTenantIdentifierResolver currentTenantIdentifierResolver(TenantResolver tenantResolver) {
		return new CurrentTenantIdentifierResolverDefault(tenantResolver);
	}

	@Bean
	@ConditionalOnMissingBean
	public MultiTenantConnectionProvider multiTenantConnectionProvider(DataSource dataSource, TenantResolver tenantResolver) {
		return new MultiTenantConnectionProviderDefault(dataSource, tenantResolver);
	}

	@Bean
	@ConditionalOnMissingBean
	public JpaVendorAdapter jpaVendorAdapter() {
		return new HibernateJpaVendorAdapter();
	}

	@Bean
	@ConditionalOnMissingBean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
			MultiTenantConnectionProvider multiTenantConnectionProvider,
			CurrentTenantIdentifierResolver tenantIdentifierResolver) {
		LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
		emfBean.setDataSource(dataSource);

		emfBean.setPackagesToScan(packageToScan);
		emfBean.setJpaVendorAdapter(jpaVendorAdapter());

		Map<String, Object> jpaProperties = new HashMap<String, Object>();
		jpaProperties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
		jpaProperties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
		jpaProperties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
		emfBean.setJpaPropertyMap(jpaProperties);
		return emfBean;
	}

	@Bean
	@ConditionalOnMissingBean
	public MultiTenantSpringLiquibase multiTenantSpringLiquibase(DataSource dataSource,
			TenantResolver tenantResolver)
			throws SQLException {
		MultiTenantSpringLiquibase multiTenantSpringLiquibase = new MultiTenantSpringLiquibase();
		multiTenantSpringLiquibase.setDataSource(dataSource);

		Collection<Tenant> tenants = tenantResolver.resolveAllTenants();
		
		List<String> schemas = new ArrayList<String>();
		
		for (Tenant tenant : tenants) {
			schemas.add(tenant.getSchemaName());
		}

		multiTenantSpringLiquibase.setSchemas(schemas);
		multiTenantSpringLiquibase.setChangeLog(changeLogPath);
		multiTenantSpringLiquibase.setContexts(multiTenancyLiquibaseContexts);
		multiTenantSpringLiquibase.setShouldRun(multiTenancyLiquibaseEnabled);

		return multiTenantSpringLiquibase;
	}

}
