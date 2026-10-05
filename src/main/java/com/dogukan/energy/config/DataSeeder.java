package com.dogukan.energy.config;

import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.entity.ConsumptionReading;
import com.dogukan.energy.entity.Meter;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.entity.Tariff;
import com.dogukan.energy.repository.BuildingZoneRepository;
import com.dogukan.energy.repository.ConsumptionReadingRepository;
import com.dogukan.energy.repository.MeterRepository;
import com.dogukan.energy.repository.TariffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Swagger üzerinden elle veri girmeden hemen deneme yapabilmek için
 * geliştirme ortamında örnek zone/meter/tariff/reading kaydı oluşturur.
 * Sadece "dev" profili aktifken çalışır, veritabanı boşsa bir kez seed eder.
 *
 * Çalıştırmak için: mvn spring-boot:run -Dspring-boot.run.profiles=dev
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final BuildingZoneRepository zoneRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final ConsumptionReadingRepository readingRepository;

    public DataSeeder(BuildingZoneRepository zoneRepository,
                       MeterRepository meterRepository,
                       TariffRepository tariffRepository,
                       ConsumptionReadingRepository readingRepository) {
        this.zoneRepository = zoneRepository;
        this.meterRepository = meterRepository;
        this.tariffRepository = tariffRepository;
        this.readingRepository = readingRepository;
    }

    @Override
    public void run(String... args) {
        if (zoneRepository.count() > 0) {
            return;
        }

        BuildingZone productionHall = newZone("Üretim Hattı A", "Ana üretim tesisi, 3 vardiyalı çalışma");
        BuildingZone officeBlock = newZone("Ofis Bloğu", "İdari ofisler ve toplantı salonları");
        zoneRepository.save(productionHall);
        zoneRepository.save(officeBlock);

        Meter activeMeter = newMeter("SM-1001", MeterStatus.ACTIVE, productionHall);
        Meter inactiveMeter = newMeter("SM-1002", MeterStatus.INACTIVE, productionHall);
        Meter faultyMeter = newMeter("SM-1003", MeterStatus.FAULTY, officeBlock);
        Meter officeActiveMeter = newMeter("SM-2001", MeterStatus.ACTIVE, officeBlock);
        meterRepository.save(activeMeter);
        meterRepository.save(inactiveMeter);
        meterRepository.save(faultyMeter);
        meterRepository.save(officeActiveMeter);

        Tariff nightTariff = newTariff("Gece Tarifesi", "1.85",
                LocalTime.of(22, 0), LocalTime.of(6, 0));
        Tariff dayTariff = newTariff("Gündüz Tarifesi", "2.40",
                LocalTime.of(6, 0), LocalTime.of(17, 0));
        Tariff peakTariff = newTariff("Puant Tarifesi", "3.10",
                LocalTime.of(17, 0), LocalTime.of(22, 0));
        tariffRepository.save(nightTariff);
        tariffRepository.save(dayTariff);
        tariffRepository.save(peakTariff);

        // SM-1001 için birkaç normal saatlik okuma (anomaliye takılmayacak şekilde)
        seedReading(activeMeter, dayTariff, "2026-09-28T08:00:00", "12.500", "30.00");
        seedReading(activeMeter, dayTariff, "2026-09-28T09:00:00", "14.000", "33.60");
        seedReading(activeMeter, peakTariff, "2026-09-28T18:00:00", "16.200", "50.22");
        seedReading(officeActiveMeter, dayTariff, "2026-09-28T09:00:00", "3.200", "7.68");
    }

    private BuildingZone newZone(String name, String description) {
        BuildingZone zone = new BuildingZone();
        zone.setName(name);
        zone.setDescription(description);
        return zone;
    }

    private Meter newMeter(String serialNumber, MeterStatus status, BuildingZone zone) {
        Meter meter = new Meter();
        meter.setSerialNumber(serialNumber);
        meter.setStatus(status);
        meter.setZone(zone);
        return meter;
    }

    private Tariff newTariff(String name, String unitPrice, LocalTime start, LocalTime end) {
        Tariff tariff = new Tariff();
        tariff.setName(name);
        tariff.setUnitPrice(new BigDecimal(unitPrice));
        tariff.setValidFrom(LocalDate.of(2026, 1, 1));
        tariff.setStartTime(start);
        tariff.setEndTime(end);
        tariff.setActive(true);
        return tariff;
    }

    private void seedReading(Meter meter, Tariff tariff, String readingTime,
                              String consumptionKwh, String totalCost) {
        ConsumptionReading reading = new ConsumptionReading();
        reading.setMeter(meter);
        reading.setTariff(tariff);
        reading.setReadingTime(LocalDateTime.parse(readingTime));
        reading.setConsumptionKwh(new BigDecimal(consumptionKwh));
        reading.setTotalCost(new BigDecimal(totalCost).setScale(2, RoundingMode.HALF_UP));
        readingRepository.save(reading);
    }
}
