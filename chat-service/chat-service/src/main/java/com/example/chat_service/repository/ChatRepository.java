package com.example.chat_service.repository;

import com.example.chat_service.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID>, JpaSpecificationExecutor<Chat> {
    List<Chat> findByHistoryId(UUID historyId);
}