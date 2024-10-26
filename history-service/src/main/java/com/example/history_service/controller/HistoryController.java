package com.example.history_service.controller;

import com.example.history_service.entity.History;
import com.example.history_service.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    @PostMapping("/")
    public History createHistory(@RequestBody History history) {
        return historyService.saveHistory(history);
    }

    @GetMapping("/user/{userId}")
    public List<History> getHistoriesByUserId(@PathVariable UUID userId) {
        return historyService.getHistoriesByUserId(userId);
    }

    @PutMapping("/{id}")
    public History updateHistory(@PathVariable UUID id, @RequestBody History updatedHistory) {
        return historyService.updateHistory(id, updatedHistory);
    }

    @DeleteMapping("/{id}")
    public String deleteHistory(@PathVariable UUID id) {
        historyService.deleteHistory(id);
        return "History deleted successfully";
    }
}
