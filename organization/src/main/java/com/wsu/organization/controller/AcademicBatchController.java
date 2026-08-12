package com.wsu.organization.controller;


import com.wsu.core.model.dto.requests.AcademicBatchRequest;
import com.wsu.core.model.dto.responses.CustomResponse;
import com.wsu.organization.service.AcademicBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batches")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_ACADEMIC_BATCHES')")
public class AcademicBatchController {
    private final AcademicBatchService academicBatchService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllAcademicBatches() {
        return ResponseEntity.ok(CustomResponse.builder()
                .data(academicBatchService.getAllAcademicBatches())
                .success(true).timeStamp(System.currentTimeMillis())
                .build());
    }


    @PostMapping("/create")
    public ResponseEntity<?>createAcademicBatch(@RequestBody AcademicBatchRequest request){

      return ResponseEntity.ok(CustomResponse.builder()
              .data(academicBatchService.createAcademicBatch(request))
              .success(true).timeStamp(System.currentTimeMillis())
              .build());

    }
}
