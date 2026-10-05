package com.dogukan.energy.repository;

import com.dogukan.energy.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    /**
     * Finds active tariffs covering the given date and time of day.
     * Time window is start-inclusive, end-exclusive. A window whose start is after its end
     * crosses midnight (e.g. 22:00-06:00); equal start and end means the whole day.
     */
    @Query("""
            SELECT t FROM Tariff t
            WHERE t.active = true
              AND t.validFrom <= :date
              AND (t.validTo IS NULL OR t.validTo >= :date)
              AND (
                    t.startTime = t.endTime
                 OR (t.startTime < t.endTime AND :time >= t.startTime AND :time < t.endTime)
                 OR (t.startTime > t.endTime AND (:time >= t.startTime OR :time < t.endTime))
              )
            ORDER BY t.validFrom DESC, t.id DESC
            """)
    List<Tariff> findApplicableTariffs(@Param("date") LocalDate date, @Param("time") LocalTime time);
}
