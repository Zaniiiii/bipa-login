package com.example.history_service.repository;

import com.example.history_service.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HistoryRepository extends JpaRepository<History, UUID>, JpaSpecificationExecutor<History> {
    List<History> findByUserId(UUID userId);

    @Query("SELECT COUNT(h) FROM History h WHERE YEAR(h.createdAt) = :year")
    long countByYear(@Param("year") int year);

    @Query("SELECT COUNT(h) FROM History h WHERE YEAR(h.createdAt) = :year AND MONTH(h.createdAt) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}