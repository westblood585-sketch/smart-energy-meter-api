package com.dogukan.energy.mapper;

import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.dto.response.MeterResponse;
import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.entity.Meter;
import org.springframework.stereotype.Component;

@Component
public class MeterMapper {

    public Meter toEntity(MeterRequest request, BuildingZone zone) {
        Meter meter = new Meter();
        updateEntity(request, meter);
        meter.setZone(zone);
        return meter;
    }

    public void updateEntity(MeterRequest request, Meter meter) {
        meter.setSerialNumber(request.serialNumber().trim());
        meter.setStatus(request.status());
    }

    public MeterResponse toResponse(Meter meter) {
        BuildingZone zone = meter.getZone();
        return new MeterResponse(
                meter.getId(),
                meter.getSerialNumber(),
                meter.getStatus(),
                zone.getId(),
                zone.getName());
    }
}
