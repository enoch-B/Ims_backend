package com.wsu.core.model.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(name = "email", nullable = false)
    private  String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "phone", nullable = false)
    private String phone;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive= Boolean.TRUE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(targetEntity = Role.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @PrePersist
    protected void onCreat(){
        createdAt= LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate(){
        lastUpdate = LocalDateTime.now();
    }
}
