package com.dogukan.energy.service;

import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.response.ConsumptionReadingResponse;
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
import com.dogukan.energy.repository.ConsumptionReadingRepository;
import com.dogukan.energy.repository.MeterRepository;
import com.dogukan.energy.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumptionReadingServiceTest {

    @Mock
    private ConsumptionReadingRepository readingRepository;
    @Mock
    private MeterRepository meterRepository;
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private com.dogukan.energy.repository.BuildingZoneRepository zoneRepository;
    @Mock
    private ConsumptionReadingMapper readingMapper;

    // Built by hand instead of @InjectMocks: the constructor's primitive
    // anomalyMultiplier parameter (from @Value) has no mock to inject, which
    // makes Mockito's constructor-injection strategy give up entirely and
    // fall back to a default constructor that this class does not have.
    private ConsumptionReadingService readingService;

    private Meter activeMeter;
    private Tariff dayTariff;

    @BeforeEach
    void setUp() throws Exception {
        readingService = new ConsumptionReadingService(
                readingRepository, meterRepository, tariffRepository, zoneRepository, readingMapper, 3);

        BuildingZone zone = new BuildingZone();
        zone.setName("Production Hall");

        activeMeter = new Meter();
        activeMeter.setSerialNumber("SM-0001");
        activeMeter.setStatus(MeterStatus.ACTIVE);
        activeMeter.setZone(zone);
        setId(activeMeter, 1L);

        dayTariff = new Tariff();
        dayTariff.setName("Day Tariff");
        dayTariff.setUnitPrice(new BigDecimal("2.0000"));
        dayTariff.setValidFrom(LocalDate.of(2026, 1, 1));
        dayTariff.setStartTime(LocalTime.of(6, 0));
        dayTariff.setEndTime(LocalTime.of(22, 0));
        dayTariff.setActive(true);
        setId(dayTariff, 1L);
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    void submit_throwsResourceNotFound_whenMeterDoesNotExist() {
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(99L, LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("10"));
        when(meterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingService.submit(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submit_throwsInactiveMeter_whenMeterIsNotActive() {
        activeMeter.setStatus(MeterStatus.FAULTY);
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("10"));
        when(meterRepository.findById(1L)).thenReturn(Optional.of(activeMeter));

        assertThatThrownBy(() -> readingService.submit(request))
                .isInstanceOf(InactiveMeterException.class);

        verify(readingRepository, never()).save(any());
    }

    @Test
    void submit_throwsAnomalousReading_whenNewValueIsAtLeastThreeTimesPrevious() {
        LocalDateTime readingTime = LocalDateTime.of(2026, 9, 28, 10, 0);
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, readingTime, new BigDecimal("30"));

        ConsumptionReading previous = new ConsumptionReading();
        previous.setConsumptionKwh(new BigDecimal("10"));

        when(meterRepository.findById(1L)).thenReturn(Optional.of(activeMeter));
        when(readingRepository.findTopByMeterIdAndReadingTimeBeforeOrderByReadingTimeDesc(1L, readingTime))
                .thenReturn(Optional.of(previous));

        assertThatThrownBy(() -> readingService.submit(request))
                .isInstanceOf(AnomalousReadingException.class);

        verify(readingRepository, never()).save(any());
    }

    @Test
    void submit_allowsReading_whenBelowAnomalyThreshold() {
        LocalDateTime readingTime = LocalDateTime.of(2026, 9, 28, 10, 0);
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, readingTime, new BigDecimal("25"));

        ConsumptionReading previous = new ConsumptionReading();
        previous.setConsumptionKwh(new BigDecimal("10"));

        when(meterRepository.findById(1L)).thenReturn(Optional.of(activeMeter));
        when(readingRepository.findTopByMeterIdAndReadingTimeBeforeOrderByReadingTimeDesc(1L, readingTime))
                .thenReturn(Optional.of(previous));
        when(tariffRepository.findApplicableTariffs(readingTime.toLocalDate(), readingTime.toLocalTime()))
                .thenReturn(List.of(dayTariff));

        ConsumptionReading saved = new ConsumptionReading();
        when(readingMapper.toEntity(any(), any(), any(), any())).thenReturn(saved);
        when(readingRepository.save(saved)).thenReturn(saved);
        when(readingMapper.toResponse(saved)).thenReturn(
                new ConsumptionReadingResponse(1L, "SM-0001", "Day Tariff", dayTariff.getUnitPrice(), readingTime, new BigDecimal("25"), new BigDecimal("50.00")));

        ConsumptionReadingResponse response = readingService.submit(request);

        assertThat(response.totalCost()).isEqualByComparingTo("50.00");
        verify(readingRepository).save(saved);
    }

    @Test
    void submit_throwsTariffNotFound_whenNoTariffCoversReadingTime() {
        LocalDateTime readingTime = LocalDateTime.of(2026, 9, 28, 10, 0);
        ConsumptionReadingRequest request = new ConsumptionReadingRequest(1L, readingTime, new BigDecimal("5"));

        when(meterRepository.findById(1L)).thenReturn(Optional.of(activeMeter));
        when(readingRepository.findTopByMeterIdAndReadingTimeBeforeOrderByReadingTimeDesc(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(tariffRepository.findApplicableTariffs(readingTime.toLocalDate(), readingTime.toLocalTime()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> readingService.submit(request))
                .isInstanceOf(TariffNotFoundException.class);

        verify(readingRepository, never()).save(any());
    }
}
