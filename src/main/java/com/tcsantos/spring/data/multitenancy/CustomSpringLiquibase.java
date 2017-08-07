package com.tcsantos.spring.data.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

public class CustomSpringLiquibase extends SpringLiquibase {

	protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
		
		try {
			c.setSchema(getDefaultSchema());
		} catch (SQLException e) {
			throw new LiquibaseException("Error setting schema for connection.", e);
		}

		return super.createLiquibase(c);
	}

}
