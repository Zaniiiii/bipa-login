package com.example.history_service.controller;

import com.example.history_service.entity.History;
import com.example.history_service.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/histories")
    public Page<History> getAllHistories(
            @RequestParam(required = false) String historyName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return historyService.getAllHistories(historyName, status, userId, pageRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/histories/{id}")
    public String deleteHistoryByAdmin(@PathVariable UUID id) {
        historyService.deleteHistory(id);
        return "History deleted successfully by admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/histories/count")
    public long getTotalHistoriesCount(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return historyService.getTotalHistoriesCount(year, month);
    }
}
