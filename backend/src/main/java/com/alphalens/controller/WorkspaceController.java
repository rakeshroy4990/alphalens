package com.alphalens.controller;

import com.alphalens.dto.response.PageResponse;
import com.alphalens.service.workspace.WorkspaceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/watchlists")
    public List<Map<String, Object>> watchlists(@RequestHeader(value = "X-User-Id", required = false) String userHeader) {
        return workspaceService.listWatchlists(user(userHeader));
    }

    @PostMapping("/watchlists")
    public Map<String, Object> createWatchlist(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestBody Map<String, String> body) {
        return workspaceService.createWatchlist(user(userHeader), body.get("name"));
    }

    @GetMapping("/watchlists/{id}")
    public Map<String, Object> watchlist(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id) {
        return workspaceService.getWatchlist(user(userHeader), id);
    }

    @PostMapping("/watchlists/{id}/items")
    public Map<String, Object> addItem(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id,
            @RequestBody Map<String, Long> body) {
        return workspaceService.addWatchlistItem(user(userHeader), id, body.get("instrumentId"));
    }

    @DeleteMapping("/watchlists/{id}/items/{itemId}")
    public Map<String, Object> removeItem(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id,
            @PathVariable long itemId) {
        return workspaceService.removeWatchlistItem(user(userHeader), id, itemId);
    }

    @PostMapping("/watchlists/{id}/reorder")
    public Map<String, Object> reorder(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id,
            @RequestBody Map<String, List<Long>> body) {
        return workspaceService.reorderWatchlist(user(userHeader), id, body.getOrDefault("itemIds", List.of()));
    }

    @GetMapping("/alerts")
    public List<Map<String, Object>> alerts(@RequestHeader(value = "X-User-Id", required = false) String userHeader) {
        return workspaceService.listAlerts(user(userHeader));
    }

    @PostMapping("/alerts")
    public Map<String, Object> createAlert(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestBody Map<String, Object> body) {
        Number instrumentId = (Number) body.get("instrumentId");
        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) body.getOrDefault("definition", Map.of());
        return workspaceService.createAlert(
                user(userHeader),
                instrumentId.longValue(),
                String.valueOf(body.get("type")),
                definition
        );
    }

    @PostMapping("/coverage-requests")
    public Map<String, Object> coverage(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestBody Map<String, Object> body) {
        Long instrumentId = body.get("instrumentId") == null ? null : ((Number) body.get("instrumentId")).longValue();
        return workspaceService.requestCoverage(user(userHeader), instrumentId, (String) body.get("symbol"));
    }

    @GetMapping("/coverage-requests")
    public List<Map<String, Object>> coverageQueue() {
        return workspaceService.coverageQueue();
    }

    @GetMapping("/user-algorithms")
    public List<Map<String, Object>> algorithms(@RequestHeader(value = "X-User-Id", required = false) String userHeader) {
        return workspaceService.listAlgorithms(user(userHeader));
    }

    @PostMapping("/user-algorithms")
    public Map<String, Object> saveAlgorithm(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestBody JsonNode body) {
        return workspaceService.saveAlgorithm(user(userHeader), body.path("name").asText(), body.path("definition"));
    }

    @PostMapping("/user-algorithms/{id}/run")
    public Map<String, Object> runAlgorithm(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id,
            @RequestBody Map<String, Long> body) {
        return workspaceService.runAlgorithm(user(userHeader), id, body.get("instrumentId"));
    }

    @PostMapping("/user-algorithms/{id}/duplicate")
    public Map<String, Object> duplicate(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id) {
        return workspaceService.duplicateAlgorithm(user(userHeader), id);
    }

    @DeleteMapping("/user-algorithms/{id}")
    public void deleteAlgorithm(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @PathVariable long id) {
        workspaceService.deleteAlgorithm(user(userHeader), id);
    }

    @PostMapping("/screener")
    public PageResponse<Map<String, Object>> screen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestBody(required = false) Map<String, Object> filters) {
        return workspaceService.screen(filters == null ? Map.of() : filters, page, size);
    }

    @PostMapping("/saved-screens")
    public Map<String, Object> saveScreen(
            @RequestHeader(value = "X-User-Id", required = false) String userHeader,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) body.getOrDefault("filters", Map.of());
        return workspaceService.saveScreen(user(userHeader), (String) body.get("name"), filters);
    }

    @GetMapping("/saved-screens")
    public List<Map<String, Object>> savedScreens(@RequestHeader(value = "X-User-Id", required = false) String userHeader) {
        return workspaceService.savedScreens(user(userHeader));
    }

    private UUID user(String header) {
        return workspaceService.userId(header);
    }
}
