package com.tcsantos.spring.data.multitenancy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "multitenancy.liquibase")
public class MultitenancyLiquibaseProperties {

	private String contexts;

	private boolean enabled = true;

	private String changelogPath;

	private final Map<String, String> tenantContexts = new HashMap<String, String>();

	public String getContexts() {
		return contexts;
	}

	public void setContexts(String contexts) {
		this.contexts = contexts;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getChangelogPath() {
		return changelogPath;
	}

	public void setChangelogPath(String changelogPath) {
		this.changelogPath = changelogPath;
	}

	public Map<String, String> getTenantContexts() {
		return tenantContexts;
	}

}
