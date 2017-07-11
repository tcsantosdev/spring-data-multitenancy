package com.tcsantos.spring.data.multitenancy;

import java.util.Collection;

public interface TenantResolver {

	Collection<Tenant> resolveAllTenants();

	Tenant resolveCurrentTenant();

	String resolveTenantSchemaName(String identifier);

}
