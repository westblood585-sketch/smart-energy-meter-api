package com.dogukan.energy.integration;

import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.dto.request.ConsumptionReadingRequest;
import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.dto.request.TariffRequest;
import com.dogukan.energy.dto.response.BuildingZoneResponse;
import com.dogukan.energy.dto.response.MeterResponse;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.exception.AnomalousReadingException;
import com.dogukan.energy.exception.InactiveMeterException;
import com.dogukan.energy.service.BuildingZoneService;
import com.dogukan.energy.service.ConsumptionReadingService;
import com.dogukan.energy.service.MeterService;
import com.dogukan.energy.service.TariffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end check that the service layer talks to a real PostgreSQL instance correctly,
 * exercising the full flow: zone -> meter -> tariff -> reading, including both business rules.
 */
@Testcontainers
@SpringBootTest
class ConsumptionReadingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("energydb_test")
            .withUsername("energy")
            .withPassword("energy");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BuildingZoneService zoneService;
    @Autowired
    private MeterService meterService;
    @Autowired
    private TariffService tariffService;
    @Autowired
    private ConsumptionReadingService readingService;

    @Test
    void submit_calculatesCost_thenRejectsAnomalousFollowUpReading() {
        BuildingZoneResponse zone = zoneService.create(new BuildingZoneRequest("Integration Zone", "test"));
        MeterResponse meter = meterService.create(new MeterRequest("SM-IT-0001", MeterStatus.ACTIVE, zone.id()));
        tariffService.create(new TariffRequest(
                "All Day", new BigDecimal("2.0000"),
                LocalDate.of(2020, 1, 1), null,
                LocalTime.of(0, 0), LocalTime.of(0, 0), true));

        var first = readingService.submit(new ConsumptionReadingRequest(
                meter.id(), LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("10")));
        assertThat(first.totalCost()).isEqualByComparingTo("20.00");

        assertThatThrownBy(() -> readingService.submit(new ConsumptionReadingRequest(
                meter.id(), LocalDateTime.of(2026, 9, 28, 11, 0), new BigDecimal("30"))))
                .isInstanceOf(AnomalousReadingException.class);
    }

    @Test
    void submit_rejectsInactiveMeter() {
        BuildingZoneResponse zone = zoneService.create(new BuildingZoneRequest("Inactive Zone", "test"));
        MeterResponse meter = meterService.create(new MeterRequest("SM-IT-0002", MeterStatus.INACTIVE, zone.id()));

        assertThatThrownBy(() -> readingService.submit(new ConsumptionReadingRequest(
                meter.id(), LocalDateTime.of(2026, 9, 28, 10, 0), new BigDecimal("5"))))
                .isInstanceOf(InactiveMeterException.class);
    }
}
