package com.example.chat_service.service;

import com.example.chat_service.entity.Chat;
import com.example.chat_service.repository.ChatRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    // Tambahkan WebClient sebagai bean
    private final WebClient webClient = WebClient.create();

    private static final String EXTERNAL_API_URL = "http://ec2-52-89-76-29.us-west-2.compute.amazonaws.com:6845/generate";

    public Chat saveChat(Chat chat) {
        // Mengirim pesan ke API eksternal dan mendapatkan respons
        String response = getResponseFromExternalApi(chat.getChat());
        chat.setResponse(response);

        return chatRepository.save(chat);
    }

    public List<Chat> getChatsByHistoryId(UUID historyId) {
        return chatRepository.findByHistoryId(historyId);
    }

    public Chat updateChat(UUID id, Chat updatedChat) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setChat(updatedChat.getChat());

        // Dapatkan respons baru dari API eksternal jika chat diubah
        String response = getResponseFromExternalApi(chat.getChat());
        chat.setResponse(response);

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

    private String getResponseFromExternalApi(String message) {
        Mono<ResponseDto> responseMono = webClient.post()
                .uri(EXTERNAL_API_URL)
                .bodyValue(new MessageDto(message))
                .retrieve()
                .bodyToMono(ResponseDto.class);

        ResponseDto responseDto = responseMono.block(); // Menggunakan block untuk mendapatkan hasil secara sinkron

        if (responseDto != null && responseDto.getResponse() != null) {
            return responseDto.getResponse();
        } else {
            return "No response from external API";
        }
    }

    // DTO untuk mengirim pesan ke API eksternal
    @Data
    @AllArgsConstructor
    private static class MessageDto {
        private String message;
    }

    // DTO untuk menerima respons dari API eksternal
    @Data
    private static class ResponseDto {
        private String response;
    }
}
