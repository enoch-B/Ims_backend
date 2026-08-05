package com.wsu.core.model.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicBatchResponse {
    private Long id;
    private String yearLabel;
    private Date startDate;
    private Date endDate;
}
