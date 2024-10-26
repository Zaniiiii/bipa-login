package com.example.history_service.service;

import com.example.history_service.entity.History;
import com.example.history_service.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    public History saveHistory(History history) {
        return historyRepository.save(history);
    }

    public List<History> getHistoriesByUserId(UUID userId) {
        return historyRepository.findByUserId(userId);
    }

    public History updateHistory(UUID id, History updatedHistory) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found"));
        history.setHistoryName(updatedHistory.getHistoryName());
        history.setStatus(updatedHistory.getStatus());
        return historyRepository.save(history);
    }

    public void deleteHistory(UUID id) {
        historyRepository.deleteById(id);
    }
}
