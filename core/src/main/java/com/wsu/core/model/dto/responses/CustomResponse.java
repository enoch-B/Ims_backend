package com.wsu.core.model.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CustomResponse<T>{

    private final T data;
    private final boolean success;
    private final long timeStamp;


}
