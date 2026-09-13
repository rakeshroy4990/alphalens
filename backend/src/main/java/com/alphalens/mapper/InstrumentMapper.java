package com.alphalens.mapper;

import com.alphalens.domain.Instrument;
import com.alphalens.dto.response.InstrumentDetailResponse;
import com.alphalens.dto.response.InstrumentSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class InstrumentMapper {

    public InstrumentSummaryResponse toSummary(Instrument instrument) {
        return new InstrumentSummaryResponse(
                instrument.getId(),
                instrument.getIsin(),
                instrument.getNseSymbol(),
                instrument.getBseSymbol(),
                instrument.getCompanyName(),
                instrument.getShortName(),
                instrument.getSector() == null ? null : instrument.getSector().getCode(),
                instrument.getIndustry() == null ? null : instrument.getIndustry().getCode(),
                instrument.getExchange() == null ? null : instrument.getExchange().getCode(),
                instrument.getStatus().name()
        );
    }

    public InstrumentDetailResponse toDetail(Instrument instrument) {
        return new InstrumentDetailResponse(
                instrument.getId(),
                instrument.getIsin(),
                instrument.getNseSymbol(),
                instrument.getBseSymbol(),
                instrument.getCompanyName(),
                instrument.getShortName(),
                instrument.getSector() == null ? null : instrument.getSector().getCode(),
                instrument.getSector() == null ? null : instrument.getSector().getName(),
                instrument.getIndustry() == null ? null : instrument.getIndustry().getCode(),
                instrument.getIndustry() == null ? null : instrument.getIndustry().getName(),
                instrument.getExchange() == null ? null : instrument.getExchange().getCode(),
                instrument.getStatus().name(),
                instrument.getUpdatedAt()
        );
    }
}
