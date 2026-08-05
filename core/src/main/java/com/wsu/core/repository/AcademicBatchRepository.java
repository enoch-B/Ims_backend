package com.wsu.core.repository;

import com.wsu.core.model.entities.AcademicBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicBatchRepository extends JpaRepository<AcademicBatch, Long> {

    boolean existsByYearLabel(String yearLabel);
}
