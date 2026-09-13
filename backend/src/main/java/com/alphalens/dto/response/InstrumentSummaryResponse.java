package com.alphalens.dto.response;

public record InstrumentSummaryResponse(
        long instrumentId,
        String isin,
        String nseSymbol,
        String bseSymbol,
        String companyName,
        String shortName,
        String sectorCode,
        String industryCode,
        String exchangeCode,
        String status
) {
}
