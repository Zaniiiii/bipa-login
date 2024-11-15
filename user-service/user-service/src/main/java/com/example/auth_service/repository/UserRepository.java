package com.example.auth_service.repository;

import com.example.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE (:year IS NULL OR FUNCTION('YEAR', u.createdAt) = :year) AND (:month IS NULL OR FUNCTION('MONTH', u.createdAt) = :month)")
    long countByRegistrationDate(@Param("year") Integer year, @Param("month") Integer month);
}
