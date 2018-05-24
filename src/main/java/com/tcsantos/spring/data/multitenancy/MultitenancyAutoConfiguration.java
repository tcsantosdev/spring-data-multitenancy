package com.tcsantos.spring.data.multitenancy;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.tcsantos.spring.data.multitenancy.hibernate.CurrentTenantIdentifierResolverDefault;
import com.tcsantos.spring.data.multitenancy.hibernate.MultiTenantConnectionProviderDefault;

@Configuration
@PropertySource("classpath:/multitenancy-default.properties")
@ComponentScan("com.tcsantos.spring.data.multitenancy")
public class MultitenancyAutoConfiguration {

	@Value("${multitenancy.entity.package.scan}")
	private String packageToScan;

	@Value("${spring.jpa.database-platform:#{null}}")
	private String databasePlatform;
	

	@Value("${spring.jpa.show-sql:#{null}}")
	private String showSql;

	@Value("${spring.jpa.hibernate.ddl-auto:#{none}}")
	private String ddlAuto;
	
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
		jpaProperties.put(org.hibernate.cfg.Environment.PHYSICAL_NAMING_STRATEGY, new ImprovedNamingStrategy());
		jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, databasePlatform);
		jpaProperties.put(org.hibernate.cfg.Environment.SHOW_SQL, showSql);
		jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, ddlAuto);
		jpaProperties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
		jpaProperties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
		emfBean.setJpaPropertyMap(jpaProperties);
		return emfBean;
	}

	@Bean
	@ConditionalOnMissingBean
	public MultiTenantSpringLiquibase multiTenantSpringLiquibase(DataSource dataSource,
			TenantResolver tenantResolver,
			MultitenancyLiquibaseProperties multitenancyLiquibaseProperties)
			throws SQLException {
		MultiTenantSpringLiquibase multiTenantSpringLiquibase = new MultiTenantSpringLiquibase(multitenancyLiquibaseProperties);
		multiTenantSpringLiquibase.setDataSource(dataSource);

		Collection<Tenant> tenants = tenantResolver.resolveAllTenants();

		multiTenantSpringLiquibase.setTenants(tenants);

		return multiTenantSpringLiquibase;
	}

}
