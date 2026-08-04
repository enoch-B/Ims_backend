package com.wsu.core.model.dto.requests;


import com.wsu.core.model.dto.responses.CreateUserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportResult {
    private List<CreateUserResponse> created;
    private List<String> failed;   // "<row content> -> <reason>"
    private int successCount;
    private int failureCount;
}