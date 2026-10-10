package com.dogukan.energy.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.dto.response.MeterResponse;
import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.entity.Meter;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.MeterMapper;
import com.dogukan.energy.repository.BuildingZoneRepository;
import com.dogukan.energy.repository.MeterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MeterService {
    private static final Logger log = LoggerFactory.getLogger(MeterService.class);


    private final MeterRepository meterRepository;
    private final BuildingZoneRepository zoneRepository;
    private final MeterMapper meterMapper;

    public MeterService(MeterRepository meterRepository, BuildingZoneRepository zoneRepository, MeterMapper meterMapper) {
        this.meterRepository = meterRepository;
        this.zoneRepository = zoneRepository;
        this.meterMapper = meterMapper;
    }

    @Transactional
    public MeterResponse create(MeterRequest request) {
        log.info("Executing MeterService#create");
        if (meterRepository.existsBySerialNumber(request.serialNumber().trim())) {
            throw new DuplicateResourceException("Meter", "serialNumber", request.serialNumber());
        }
        BuildingZone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("BuildingZone", request.zoneId()));
        Meter saved = meterRepository.save(meterMapper.toEntity(request, zone));
        return meterMapper.toResponse(saved);
    }

    @Transactional
    public MeterResponse update(Long id, MeterRequest request) {
        log.info("Executing MeterService#update");
        Meter meter = findEntity(id);
        if (!meter.getSerialNumber().equalsIgnoreCase(request.serialNumber().trim())
                && meterRepository.existsBySerialNumber(request.serialNumber().trim())) {
            throw new DuplicateResourceException("Meter", "serialNumber", request.serialNumber());
        }
        if (!meter.getZone().getId().equals(request.zoneId())) {
            BuildingZone zone = zoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("BuildingZone", request.zoneId()));
            meter.setZone(zone);
        }
        meterMapper.updateEntity(request, meter);
        return meterMapper.toResponse(meter);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Executing MeterService#delete");
        meterRepository.delete(findEntity(id));
    }

    public MeterResponse getById(Long id) {
        log.info("Executing MeterService#getById");
        return meterMapper.toResponse(findEntity(id));
    }

    public Page<MeterResponse> getByZone(Long zoneId, Pageable pageable) {
        log.info("Executing MeterService#getByZone");
        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("BuildingZone", zoneId);
        }
        return meterRepository.findByZoneId(zoneId, pageable).map(meterMapper::toResponse);
    }

    public Page<MeterResponse> getAll(Pageable pageable) {
        log.info("Executing MeterService#getAll");
        return meterRepository.findAll(pageable).map(meterMapper::toResponse);
    }

    private Meter findEntity(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", id));
    }
}
