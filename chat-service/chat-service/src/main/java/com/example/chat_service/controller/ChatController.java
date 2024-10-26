package com.example.chat_service.controller;

import com.example.chat_service.entity.Chat;
import com.example.chat_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/")
    public Chat createChat(@RequestBody Chat chat) {
        return chatService.saveChat(chat);
    }

    @GetMapping("/history/{historyId}")
    public List<Chat> getChatsByHistoryId(@PathVariable UUID historyId) {
        return chatService.getChatsByHistoryId(historyId);
    }

    @PutMapping("/{id}")
    public Chat updateChat(@PathVariable UUID id, @RequestBody Chat updatedChat) {
        return chatService.updateChat(id, updatedChat);
    }

    @DeleteMapping("/{id}")
    public String deleteChat(@PathVariable UUID id) {
        chatService.deleteChat(id);
        return "Chat deleted successfully";
    }
}
