package com.dogukan.energy.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.response.ConsumptionReadingResponse;
import com.dogukan.energy.dto.response.ConsumptionReportResponse;
import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.entity.ConsumptionReading;
import com.dogukan.energy.entity.Meter;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.entity.Tariff;
import com.dogukan.energy.exception.AnomalousReadingException;
import com.dogukan.energy.exception.InactiveMeterException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.exception.TariffNotFoundException;
import com.dogukan.energy.mapper.ConsumptionReadingMapper;
import com.dogukan.energy.repository.BuildingZoneRepository;
import com.dogukan.energy.repository.ConsumptionReadingRepository;
import com.dogukan.energy.repository.ConsumptionSummary;
import com.dogukan.energy.repository.MeterRepository;
import com.dogukan.energy.repository.TariffRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsumptionReadingService {
    private static final Logger log = LoggerFactory.getLogger(ConsumptionReadingService.class);


    private final ConsumptionReadingRepository readingRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final BuildingZoneRepository zoneRepository;
    private final ConsumptionReadingMapper readingMapper;
    private final int anomalyMultiplier;

    public ConsumptionReadingService(ConsumptionReadingRepository readingRepository,
                                      MeterRepository meterRepository,
                                      TariffRepository tariffRepository,
                                      BuildingZoneRepository zoneRepository,
                                      ConsumptionReadingMapper readingMapper,
                                      @Value("${energy.anomaly-multiplier:3}") int anomalyMultiplier) {
        this.readingRepository = readingRepository;
        this.meterRepository = meterRepository;
        this.tariffRepository = tariffRepository;
        this.zoneRepository = zoneRepository;
        this.readingMapper = readingMapper;
        this.anomalyMultiplier = anomalyMultiplier;
    }

    @Transactional
    public ConsumptionReadingResponse submit(ConsumptionReadingRequest request) {
        log.info("Executing ConsumptionReadingService#submit");
        Meter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", request.meterId()));

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new InactiveMeterException(meter.getSerialNumber(), meter.getStatus());
        }

        validateNotAnomalous(meter, request);

        Tariff tariff = findApplicableTariff(request.readingTime());
        BigDecimal totalCost = request.consumptionKwh()
                .multiply(tariff.getUnitPrice())
                .setScale(2, RoundingMode.HALF_UP);

        ConsumptionReading reading = readingMapper.toEntity(request, meter, tariff, totalCost);
        ConsumptionReading saved = readingRepository.save(reading);
        return readingMapper.toResponse(saved);
    }

    private void validateNotAnomalous(Meter meter, ConsumptionReadingRequest request) {
        readingRepository
                .findTopByMeterIdAndReadingTimeBeforeOrderByReadingTimeDesc(meter.getId(), request.readingTime())
                .ifPresent(previous -> {
                    BigDecimal previousValue = previous.getConsumptionKwh();
                    if (previousValue.signum() > 0) {
                        BigDecimal threshold = previousValue.multiply(BigDecimal.valueOf(anomalyMultiplier));
                        if (request.consumptionKwh().compareTo(threshold) >= 0) {
                            throw new AnomalousReadingException(
                                    meter.getSerialNumber(), previousValue, request.consumptionKwh(), anomalyMultiplier);
                        }
                    }
                });
    }

    private Tariff findApplicableTariff(LocalDateTime readingTime) {
        List<Tariff> candidates = tariffRepository.findApplicableTariffs(readingTime.toLocalDate(), readingTime.toLocalTime());
        if (candidates.isEmpty()) {
            throw new TariffNotFoundException(readingTime);
        }
        return candidates.get(0);
    }

    public Page<ConsumptionReadingResponse> getByMeter(Long meterId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Executing ConsumptionReadingService#getByMeter");
        if (!meterRepository.existsById(meterId)) {
            throw new ResourceNotFoundException("Meter", meterId);
        }
        return readingRepository.findByMeterIdAndReadingTimeBetween(meterId, from, to, pageable)
                .map(readingMapper::toResponse);
    }

    public ConsumptionReportResponse getMeterReport(Long meterId, LocalDateTime from, LocalDateTime to) {
        log.info("Executing ConsumptionReadingService#getMeterReport");
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", meterId));
        ConsumptionSummary summary = readingRepository.summarizeByMeter(meterId, from, to);
        return readingMapper.toReport(meter.getId(), meter.getSerialNumber(), from, to, summary);
    }

    public ConsumptionReportResponse getZoneReport(Long zoneId, LocalDateTime from, LocalDateTime to) {
        log.info("Executing ConsumptionReadingService#getZoneReport");
        BuildingZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("BuildingZone", zoneId));
        ConsumptionSummary summary = readingRepository.summarizeByZone(zoneId, from, to);
        return readingMapper.toReport(zone.getId(), zone.getName(), from, to, summary);
    }
}
