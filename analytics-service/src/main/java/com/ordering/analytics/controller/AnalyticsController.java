package com.ordering.analytics.controller;

import com.ordering.analytics.dto.AnalyticsSummaryDto;
import com.ordering.analytics.dto.OrderEventDto;
import com.ordering.analytics.service.AggregationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AggregationService aggregationService;

    public AnalyticsController(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        return ResponseEntity.ok(aggregationService.getSummary());
    }

    @GetMapping("/events")
    public ResponseEntity<List<OrderEventDto>> getEvents() {
        return ResponseEntity.ok(aggregationService.getRecentEvents());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics() {
        return aggregationService.registerSseEmitter();
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        aggregationService.reset();
        return ResponseEntity.ok(Map.of("message", "Analytics aggregated metrics successfully reset."));
    }
}
