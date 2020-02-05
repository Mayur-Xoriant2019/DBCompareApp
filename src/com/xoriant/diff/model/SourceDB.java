package com.xoriant.diff.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SourceDB extends ConnectionInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "SourceDB [getDatabase()=" + getDatabase() + ", getDatabaseName()=" + getDatabaseName()
				+ ", getServer()=" + getServer() + ", getPort()=" + getPort() + ", getUsername()=" + getUsername()
				+ ", getPassword()=" + getPassword() + ", isChanged()=" + isChanged() + ", getTableInfo()="
				+ getTableInfo()+ "]";
	}
	
}
