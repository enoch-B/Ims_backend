package com.wsu.core.Exception;

public class AcademicBatchServiceException extends ImsBaseException {
    private String errorCode = "0";

    public AcademicBatchServiceException() {
        super();
    }

    public AcademicBatchServiceException(String message) {
        super(message);
    }

    public AcademicBatchServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AcademicBatchServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcademicBatchServiceException(Throwable cause) {
        super(cause);
    }

    protected AcademicBatchServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
