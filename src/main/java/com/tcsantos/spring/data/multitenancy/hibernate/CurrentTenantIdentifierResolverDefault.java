package com.tcsantos.spring.data.multitenancy.hibernate;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import com.tcsantos.spring.data.multitenancy.Tenant;
import com.tcsantos.spring.data.multitenancy.TenantResolver;

public class CurrentTenantIdentifierResolverDefault implements CurrentTenantIdentifierResolver {

	private TenantResolver tenantResolver;
	
	public CurrentTenantIdentifierResolverDefault(TenantResolver tenantResolver) {
		this.tenantResolver = tenantResolver;
	}

	@Override
	public String resolveCurrentTenantIdentifier() {
		Tenant tenant = tenantResolver.resolveCurrentTenant();

		return tenant != null ? tenant.getIdentifier() : null;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}

}
