package com.dogukan.energy.repository;

import com.dogukan.energy.entity.Meter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    Optional<Meter> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    Page<Meter> findByZoneId(Long zoneId, Pageable pageable);
}
