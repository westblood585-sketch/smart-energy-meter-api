package com.dogukan.energy.mapper;

import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.response.ConsumptionReadingResponse;
import com.dogukan.energy.dto.response.ConsumptionReportResponse;
import com.dogukan.energy.entity.ConsumptionReading;
import com.dogukan.energy.entity.Meter;
import com.dogukan.energy.entity.Tariff;
import com.dogukan.energy.repository.ConsumptionSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ConsumptionReadingMapper {

    public ConsumptionReading toEntity(ConsumptionReadingRequest request, Meter meter, Tariff tariff, BigDecimal totalCost) {
        ConsumptionReading reading = new ConsumptionReading();
        reading.setMeter(meter);
        reading.setTariff(tariff);
        reading.setReadingTime(request.readingTime());
        reading.setConsumptionKwh(request.consumptionKwh());
        reading.setTotalCost(totalCost);
        return reading;
    }

    public ConsumptionReadingResponse toResponse(ConsumptionReading reading) {
        Tariff tariff = reading.getTariff();
        return new ConsumptionReadingResponse(
                reading.getId(),
                reading.getMeter().getSerialNumber(),
                tariff.getName(),
                tariff.getUnitPrice(),
                reading.getReadingTime(),
                reading.getConsumptionKwh(),
                reading.getTotalCost());
    }

    public ConsumptionReportResponse toReport(Long referenceId, String referenceName,
                                              LocalDateTime from, LocalDateTime to,
                                              ConsumptionSummary summary) {
        return new ConsumptionReportResponse(
                referenceId,
                referenceName,
                from,
                to,
                orZero(summary.getTotalConsumption()),
                orZero(summary.getTotalCost()),
                summary.getReadingCount() == null ? 0L : summary.getReadingCount());
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
