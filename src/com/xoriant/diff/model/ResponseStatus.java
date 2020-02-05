package com.xoriant.diff.model;

public class ResponseStatus {

	private String code;
    private String severity;
    private String message;
    
    
	public ResponseStatus(String code, String severity, String message) {
		super();
		this.code = code;
		this.severity = severity;
		this.message = message;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "ResponseStatus [code=" + code + ", severity=" + severity + ", message=" + message + "]";
	}
    
}
