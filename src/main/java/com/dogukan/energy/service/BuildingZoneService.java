package com.dogukan.energy.service;

import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.dto.response.BuildingZoneResponse;
import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.BuildingZoneMapper;
import com.dogukan.energy.repository.BuildingZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BuildingZoneService {

    private final BuildingZoneRepository zoneRepository;
    private final BuildingZoneMapper zoneMapper;

    public BuildingZoneService(BuildingZoneRepository zoneRepository, BuildingZoneMapper zoneMapper) {
        this.zoneRepository = zoneRepository;
        this.zoneMapper = zoneMapper;
    }

    @Transactional
    public BuildingZoneResponse create(BuildingZoneRequest request) {
        if (zoneRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new DuplicateResourceException("BuildingZone", "name", request.name());
        }
        BuildingZone saved = zoneRepository.save(zoneMapper.toEntity(request));
        return zoneMapper.toResponse(saved);
    }

    @Transactional
    public BuildingZoneResponse update(Long id, BuildingZoneRequest request) {
        BuildingZone zone = findEntity(id);
        if (!zone.getName().equalsIgnoreCase(request.name().trim())
                && zoneRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new DuplicateResourceException("BuildingZone", "name", request.name());
        }
        zoneMapper.updateEntity(request, zone);
        return zoneMapper.toResponse(zone);
    }

    @Transactional
    public void delete(Long id) {
        BuildingZone zone = findEntity(id);
        zoneRepository.delete(zone);
    }

    public BuildingZoneResponse getById(Long id) {
        return zoneMapper.toResponse(findEntity(id));
    }

    public Page<BuildingZoneResponse> getAll(Pageable pageable) {
        return zoneRepository.findAll(pageable).map(zoneMapper::toResponse);
    }

    private BuildingZone findEntity(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BuildingZone", id));
    }
}
