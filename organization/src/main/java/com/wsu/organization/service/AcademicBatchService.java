package com.wsu.organization.service;

import com.wsu.core.Exception.AcademicBatchServiceException;
import com.wsu.core.model.dto.requests.AcademicBatchRequest;
import com.wsu.core.model.dto.responses.AcademicBatchResponse;
import com.wsu.core.model.entities.AcademicBatch;
import com.wsu.core.repository.AcademicBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcademicBatchService {
    private  final AcademicBatchRepository academicBatchesRepository;

    public AcademicBatchResponse createAcademicBatch(AcademicBatchRequest academicBatchRequest) {
        log.info("[createAcademicBatch] Creating academic batch: {}", academicBatchRequest);
        try{
            if(academicBatchesRepository.existsByYearLabel(academicBatchRequest.getYearLabel())){
                log.error("[createAcademicBatch] Academic batch with year label {} already exists", academicBatchRequest.getYearLabel());
                throw new AcademicBatchServiceException("Academic batch with year label " + academicBatchRequest.getYearLabel() + " already exists");
            }
            AcademicBatch academicBatch=academicBatchesRepository.save( AcademicBatch.builder()
                            .yearLabel(academicBatchRequest.getYearLabel())
                            .startDate(academicBatchRequest.getStartDate())
                            .endDate(academicBatchRequest.getEndDate())
                            .build());
            return AcademicBatchResponse.builder()
                    .id(academicBatch.getId())
                    .yearLabel(academicBatch.getYearLabel())
                    .startDate(academicBatch.getStartDate())
                    .endDate(academicBatch.getEndDate())
                    .build();
        }catch (Exception e){
            log.error("[createAcademicBatch] Error creating academic batch: {}", e.getMessage());
            throw new AcademicBatchServiceException("Error creating academic batch: " + e.getMessage());
        }
    }


    public List<AcademicBatchResponse> getAllAcademicBatches() {
        log.info("[getAllAcademicBatches] Fetching all academic batches");
        try{
            List<AcademicBatch> academicBatches=academicBatchesRepository.findAll();
            return academicBatches.stream()
                    .map(this::mapToResponse)
                    .toList();
        }catch (Exception e){
            log.error("[getAllAcademicBatches] Error fetching academic batches: {}", e.getMessage());
            throw new AcademicBatchServiceException("Error fetching academic batches: " + e.getMessage());
        }
    }

    private AcademicBatchResponse mapToResponse(AcademicBatch academicBatch){
        return AcademicBatchResponse.builder()
                .id(academicBatch.getId())
                .yearLabel(academicBatch.getYearLabel())
                .startDate(academicBatch.getStartDate())
                .endDate(academicBatch.getEndDate())
                .build();
    }


}
