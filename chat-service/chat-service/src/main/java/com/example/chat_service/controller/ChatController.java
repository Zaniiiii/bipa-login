package com.example.chat_service.controller;

import com.example.chat_service.dto.ChatRequestDto;
import com.example.chat_service.entity.Chat;
import com.example.chat_service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/")
    public Chat createChat(@Valid @RequestBody ChatRequestDto chatRequestDto) {
        Chat chat = Chat.builder()
                .chat(chatRequestDto.getChat())
                .historyId(chatRequestDto.getHistoryId())
                .build();

        return chatService.saveChat(chat);
    }

    @GetMapping("/history/{historyId}")
    public List<Chat> getChatsByHistoryId(@PathVariable UUID historyId) {
        return chatService.getChatsByHistoryId(historyId);
    }

    @PutMapping("/{id}")
    public Chat updateChat(@PathVariable UUID id, @Valid @RequestBody ChatRequestDto chatRequestDto) {
        Chat updatedChat = Chat.builder()
                .chat(chatRequestDto.getChat())
                .historyId(chatRequestDto.getHistoryId())
                .build();

        return chatService.updateChat(id, updatedChat);
    }

    @DeleteMapping("/{id}")
    public String deleteChat(@PathVariable UUID id) {
        chatService.deleteChat(id);
        return "Chat deleted successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chats")
    public Page<Chat> getAllChats(
            @RequestParam(required = false) String chatContent,
            @RequestParam(required = false) String responseContent,
            @RequestParam(required = false) UUID historyId,
            @RequestParam(required = false) Boolean isEdited,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return chatService.getAllChats(chatContent, responseContent, historyId, isEdited, pageRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/chats/{id}")
    public String deleteChatByAdmin(@PathVariable UUID id) {
        chatService.deleteChat(id);
        return "Chat deleted successfully by admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/chats/count")
    public long getChatCount(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return chatService.getChatCount(year, month);
    }
}
