package com.xoriant.diff.model;

import java.io.Serializable;

public class DKRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SourceDB sourcedb;
	private DestinationDB destinationdb;
	
	
	public DestinationDB getDestinationdb() {
		return destinationdb;
	}
	public void setDestinationdb(DestinationDB destinationdb) {
		this.destinationdb = destinationdb;
	}
	public SourceDB getSourcedb() {
		return sourcedb;
	}
	public void setSourcedb(SourceDB sourcedb) {
		this.sourcedb = sourcedb;
	}
	
	@Override
	public String toString() {
		return "DBInfo [sourcedb=" + sourcedb + ", destinationdb=" + destinationdb + "]";
	}
	

}
