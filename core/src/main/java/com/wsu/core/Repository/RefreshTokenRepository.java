package com.wsu.core.Repository;

import com.wsu.core.model.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    RefreshToken findByTokenHash(String tokenHash);
}
