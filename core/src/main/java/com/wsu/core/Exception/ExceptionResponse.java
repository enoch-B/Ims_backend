package com.wsu.core.Exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExceptionResponse {

    @JsonProperty("message")
    private String message;
    @JsonProperty("uri")
    private String uri;
    @JsonProperty("status")
    private int status;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("timestamp")
    private final Long timestamp = System.currentTimeMillis();

}