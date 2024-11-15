package com.example.auth_service.repository;

import com.example.auth_service.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    @Query("SELECT COUNT(DISTINCT lh.userId) FROM LoginHistory lh WHERE lh.loginAt >= :since")
    long countDistinctByUserIdAndLoginAtAfter(LocalDateTime since);

    @Query("SELECT lh.userId, MAX(lh.loginAt) as lastLoginAt FROM LoginHistory lh GROUP BY lh.userId ORDER BY lastLoginAt DESC")
    List<Object[]> findRecentUniqueLogins(Pageable pageable);
}
