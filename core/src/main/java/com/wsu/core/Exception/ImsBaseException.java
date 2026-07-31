package com.wsu.core.Exception;

import lombok.Getter;

@Getter
public class ImsBaseException extends RuntimeException {

    private String errorCode = "0";

    public ImsBaseException() {
        super();
    }

    public ImsBaseException(String message) {
        super(message);
    }

    public ImsBaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ImsBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImsBaseException(Throwable cause) {
        super(cause);
    }

    protected ImsBaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
