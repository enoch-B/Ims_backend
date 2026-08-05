package com.wsu.core.model.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademicBatchRequest {
    private String yearLabel;
    private Date startDate;
    private Date endDate;
}
