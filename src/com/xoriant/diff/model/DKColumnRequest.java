package com.xoriant.diff.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DKColumnRequest {

	private String databaseFlavor;
	private String database;
	private String server;
	private Long port;
	private String username;
	private String password;
	private String tableName;
	
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getDatabaseFlavor() {
		return databaseFlavor;
	}
	public void setDatabaseFlavor(String databaseFlavor) {
		this.databaseFlavor = databaseFlavor;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public Long getPort() {
		return port;
	}
	public void setPort(Long port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	public String toString() {
		return "DKColumnRequest [database=" + database + ", databaseFlavor=" + databaseFlavor + ", server=" + server
				+ ", port=" + port + ", username=" + username + ", password=" + password + ", tableName=" + tableName
				+ "]";
	}
	
	public boolean notNull() {
		
		if(this.database.isEmpty() || this.databaseFlavor.isEmpty() || this.server.isEmpty() || this.port == null
				|| this.username.isEmpty() || this.password.isEmpty() || this.tableName.isEmpty())
			return false;
		
		return true;
	}
	
	
	
}
