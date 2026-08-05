package com.wsu.app.exception;


import com.wsu.core.Exception.DepartmentServiceException;
import com.wsu.core.Exception.ExceptionResponse;
import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.Exception.UserServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {UserServiceException.class, DepartmentServiceException.class})
    public ResponseEntity<ExceptionResponse> handleServiceException(Exception exception, WebRequest request) {
        log.error("[handleServiceException]: service exception: {}", exception.getMessage());
        ExceptionResponse error = new ExceptionResponse();
        error.setMessage(exception.getMessage());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setUri(((ServletWebRequest) request).getRequest().getRequestURI());
        error.setErrorCode("SERVICE_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    @ExceptionHandler(value = {AccessDeniedException.class, SignatureException.class})
    public ResponseEntity<ExceptionResponse> handleAccessDenied(Exception exception, WebRequest request) {

        log.error("[handleAccessDenied]: access denied: {}", exception.getMessage());

        ExceptionResponse error = new ExceptionResponse();
        error.setMessage("Access Denied");
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setUri(((ServletWebRequest) request).getRequest().getRequestURI());
        error.setErrorCode("UNAUTHORIZED");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ExceptionResponse error = new  ExceptionResponse();
        error.setMessage(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setUri(request.getRequestURI());
        error.setErrorCode("RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(
            Exception exception,
            WebRequest request) {

        log.error("[handleGenericException]: Unexpected error at: {}",
                ((ServletWebRequest) request).getRequest().getRequestURL(),
                exception
        );

        ExceptionResponse error = new ExceptionResponse();
        error.setMessage("An unexpected error occurred");
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setUri(((ServletWebRequest) request).getRequest().getRequestURI());
        error.setErrorCode("INTERNAL_SERVER_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
