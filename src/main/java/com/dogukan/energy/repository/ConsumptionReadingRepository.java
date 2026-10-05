package com.dogukan.energy.repository;

import com.dogukan.energy.entity.ConsumptionReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConsumptionReadingRepository extends JpaRepository<ConsumptionReading, Long> {

    Optional<ConsumptionReading> findTopByMeterIdAndReadingTimeBeforeOrderByReadingTimeDesc(
            Long meterId, LocalDateTime readingTime);

    Page<ConsumptionReading> findByMeterIdAndReadingTimeBetween(
            Long meterId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("""
            SELECT SUM(r.consumptionKwh) AS totalConsumption,
                   SUM(r.totalCost) AS totalCost,
                   COUNT(r) AS readingCount
            FROM ConsumptionReading r
            WHERE r.meter.id = :meterId
              AND r.readingTime BETWEEN :from AND :to
            """)
    ConsumptionSummary summarizeByMeter(
            @Param("meterId") Long meterId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT SUM(r.consumptionKwh) AS totalConsumption,
                   SUM(r.totalCost) AS totalCost,
                   COUNT(r) AS readingCount
            FROM ConsumptionReading r
            WHERE r.meter.zone.id = :zoneId
              AND r.readingTime BETWEEN :from AND :to
            """)
    ConsumptionSummary summarizeByZone(
            @Param("zoneId") Long zoneId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
