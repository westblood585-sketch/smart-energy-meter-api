package com.dogukan.energy.mapper;

import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.dto.response.BuildingZoneResponse;
import com.dogukan.energy.entity.BuildingZone;
import org.springframework.stereotype.Component;

@Component
public class BuildingZoneMapper {

    public BuildingZone toEntity(BuildingZoneRequest request) {
        BuildingZone zone = new BuildingZone();
        updateEntity(request, zone);
        return zone;
    }

    public void updateEntity(BuildingZoneRequest request, BuildingZone zone) {
        zone.setName(request.name().trim());
        zone.setDescription(request.description());
    }

    public BuildingZoneResponse toResponse(BuildingZone zone) {
        return new BuildingZoneResponse(zone.getId(), zone.getName(), zone.getDescription());
    }
}
