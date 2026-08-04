package com.wsu.core.repository;

import com.wsu.core.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserName(String username);
    boolean existsByUserName(String username);
}
