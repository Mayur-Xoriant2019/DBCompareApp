package com.xoriant.diff.exception;

public class DiffKitServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorCode;
	private String errorMessage;
	
	public DiffKitServiceException() {
        super();
    }
	
	public DiffKitServiceException(String errorMessage) {
		super(errorMessage);
	}
	
	public DiffKitServiceException(Exception ex) {
		super(ex);
	}
	
	
	 public DiffKitServiceException(String pErrorCode, String pMessage) {
        this(pMessage);
        setErrorCode(pErrorCode);
    }

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
