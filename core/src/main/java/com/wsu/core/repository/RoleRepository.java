package com.wsu.core.repository;

import com.wsu.core.model.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String roleName);
}
