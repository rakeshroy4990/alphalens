package com.alphalens.controller;

import com.alphalens.dto.response.InstrumentDetailResponse;
import com.alphalens.dto.response.InstrumentSummaryResponse;
import com.alphalens.dto.response.PageResponse;
import com.alphalens.service.InstrumentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public PageResponse<InstrumentSummaryResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return instrumentService.list(page, size);
    }

    @GetMapping("/search")
    public PageResponse<InstrumentSummaryResponse> search(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return instrumentService.search(q, page, size);
    }

    @GetMapping("/{instrumentId}")
    public InstrumentDetailResponse get(@PathVariable long instrumentId) {
        return instrumentService.get(instrumentId);
    }
}
