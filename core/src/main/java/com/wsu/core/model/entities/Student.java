package com.wsu.core.model.entities;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "student")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {

    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId   // tells Hibernate: this entity's PK IS the referenced entity's PK
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private AcademicBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private User advisor;


}
