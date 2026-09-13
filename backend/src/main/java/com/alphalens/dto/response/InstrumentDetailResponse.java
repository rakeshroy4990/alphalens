package com.alphalens.dto.response;

import java.time.Instant;

public record InstrumentDetailResponse(
        long instrumentId,
        String isin,
        String nseSymbol,
        String bseSymbol,
        String companyName,
        String shortName,
        String sectorCode,
        String sectorName,
        String industryCode,
        String industryName,
        String exchangeCode,
        String status,
        Instant updatedAt
) {
}
