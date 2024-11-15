package com.example.history_service.service;

import com.example.history_service.entity.History;
import com.example.history_service.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<History> getAllHistories(String historyName, String status, UUID userId, Pageable pageable) {
        Specification<History> specification = Specification.where(null);

        if (historyName != null && !historyName.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("historyName")), "%" + historyName.toLowerCase() + "%"));
        }

        if (status != null && !status.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        }

        if (userId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("userId"), userId));
        }

        return historyRepository.findAll(specification, pageable);
    }

    public long getTotalHistoriesCount(Integer year, Integer month) {
        if (year != null && month != null) {
            return historyRepository.countByYearAndMonth(year, month);
        } else if (year != null) {
            return historyRepository.countByYear(year);
        }
        return historyRepository.count();
    }

}
