package com.example.chat_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatRequestDto {
    @NotBlank(message = "Chat message cannot be blank")
    private String chat;

    @NotNull(message = "History ID cannot be null")
    private UUID historyId;
}
