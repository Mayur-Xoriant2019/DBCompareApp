package com.xoriant.diff.model;

public class DKResponse{
	
	private String sourceValue;
	private String destinationValue;
	private ResponseStatus ResponseStatus;
	
	
	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}

	public String getDestinationValue() {
		return destinationValue;
	}

	public void setDestinationValue(String destinationValue) {
		this.destinationValue = destinationValue;
	}

	public ResponseStatus getResponseStatus() {
		return ResponseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		ResponseStatus = responseStatus;
	}

	@Override
	public String toString() {
		return "DBUpdateResponse [sourceValue=" + sourceValue + ", destinationValue=" + destinationValue
				+ ", ResponseStatus=" + ResponseStatus + "]";
	}

	

}
