package com.example.history_service.repository;

import com.example.history_service.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HistoryRepository extends JpaRepository<History, UUID> {
    List<History> findByUserId(UUID userId);
}
