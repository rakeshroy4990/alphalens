package com.alphalens.service;

import com.alphalens.domain.Instrument;
import com.alphalens.dto.response.InstrumentDetailResponse;
import com.alphalens.dto.response.InstrumentSummaryResponse;
import com.alphalens.dto.response.PageResponse;
import com.alphalens.exception.InvalidRequestException;
import com.alphalens.exception.ResourceNotFoundException;
import com.alphalens.mapper.InstrumentMapper;
import com.alphalens.repository.InstrumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentService {

    static final int MAX_PAGE_SIZE = 100;

    private final InstrumentRepository instrumentRepository;
    private final InstrumentMapper instrumentMapper;

    public InstrumentService(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<InstrumentSummaryResponse> list(int page, int size) {
        Page<Instrument> result = instrumentRepository.findAll(pageRequest(page, size));
        return toPage(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<InstrumentSummaryResponse> search(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new InvalidRequestException("Search query q is required");
        }
        String trimmed = query.trim();
        if (trimmed.length() > 64) {
            throw new InvalidRequestException("Search query is too long");
        }
        Page<Instrument> result = instrumentRepository.search(trimmed, pageRequest(page, size));
        return toPage(result);
    }

    @Transactional(readOnly = true)
    public InstrumentDetailResponse get(long instrumentId) {
        Instrument instrument = require(instrumentId);
        return instrumentMapper.toDetail(instrument);
    }

    @Transactional(readOnly = true)
    public Instrument require(long instrumentId) {
        return instrumentRepository.findDetailById(instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found: " + instrumentId));
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, size, Sort.by("companyName").ascending());
    }

    private PageResponse<InstrumentSummaryResponse> toPage(Page<Instrument> result) {
        return new PageResponse<>(
                result.getContent().stream().map(instrumentMapper::toSummary).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
