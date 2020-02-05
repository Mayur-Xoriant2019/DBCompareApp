package com.xoriant.diff.model;

import java.util.List;

public class DKColumnResponse {
	
	List<String> columns;
	ResponseStatus responseStatus;

	
	
	public DKColumnResponse(List<String> columns, ResponseStatus responseStatus) {
		super();
		this.columns = columns;
		this.responseStatus = responseStatus;
	}

	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "DKColumnResponse [columns=" + columns + ", responseStatus=" + responseStatus + "]";
	}
	

}
