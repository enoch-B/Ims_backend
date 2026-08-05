package com.wsu.core.Exception;

public class DepartmentServiceException extends ImsBaseException {
    private String errorCode = "0";

    public DepartmentServiceException() {
        super();
    }

    public DepartmentServiceException(String message) {
        super(message);
    }

    public DepartmentServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DepartmentServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DepartmentServiceException(Throwable cause) {
        super(cause);
    }

    protected DepartmentServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
