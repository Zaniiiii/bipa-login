package com.example.chat_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String chat;

    @Column(columnDefinition = "TEXT")
    private String response;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isEdited;

    private UUID historyId;  // Foreign Key to History

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        isEdited = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        isEdited = true;
    }
}
