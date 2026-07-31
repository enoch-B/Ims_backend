package com.wsu.core.model.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "academic_batch")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AcademicBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "year_label", nullable = false)
    private String yearLabel;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
