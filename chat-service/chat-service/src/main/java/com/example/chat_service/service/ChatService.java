package com.example.chat_service.service;

import com.example.chat_service.entity.Chat;
import com.example.chat_service.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public List<Chat> getChatsByHistoryId(UUID historyId) {
        return chatRepository.findByHistoryId(historyId);
    }

    public Chat updateChat(UUID id, Chat updatedChat) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setChat(updatedChat.getChat());
        chat.setResponse(updatedChat.getResponse());
        return chatRepository.save(chat);
    }

    public void deleteChat(UUID id) {
        chatRepository.deleteById(id);
    }

    public Page<Chat> getAllChats(String chatContent, String responseContent, UUID historyId, Boolean isEdited, Pageable pageable) {
        Specification<Chat> specification = Specification.where(null);

        if (chatContent != null && !chatContent.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("chat")), "%" + chatContent.toLowerCase() + "%"));
        }

        if (responseContent != null && !responseContent.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("response")), "%" + responseContent.toLowerCase() + "%"));
        }

        if (historyId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("historyId"), historyId));
        }

        if (isEdited != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("isEdited"), isEdited));
        }

        return chatRepository.findAll(specification, pageable);
    }
}
