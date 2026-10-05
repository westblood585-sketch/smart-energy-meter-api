package com.dogukan.energy.mapper;

import com.dogukan.energy.dto.request.TariffRequest;
import com.dogukan.energy.dto.response.TariffResponse;
import com.dogukan.energy.entity.Tariff;
import org.springframework.stereotype.Component;

@Component
public class TariffMapper {

    public Tariff toEntity(TariffRequest request) {
        Tariff tariff = new Tariff();
        updateEntity(request, tariff);
        return tariff;
    }

    public void updateEntity(TariffRequest request, Tariff tariff) {
        tariff.setName(request.name().trim());
        tariff.setUnitPrice(request.unitPrice());
        tariff.setValidFrom(request.validFrom());
        tariff.setValidTo(request.validTo());
        tariff.setStartTime(request.startTime());
        tariff.setEndTime(request.endTime());
        tariff.setActive(Boolean.TRUE.equals(request.active()));
    }

    public TariffResponse toResponse(Tariff tariff) {
        return new TariffResponse(
                tariff.getId(),
                tariff.getName(),
                tariff.getUnitPrice(),
                tariff.getValidFrom(),
                tariff.getValidTo(),
                tariff.getStartTime(),
                tariff.getEndTime(),
                tariff.isActive());
    }
}
