package com.dogukan.energy.repository;

import com.dogukan.energy.entity.BuildingZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingZoneRepository extends JpaRepository<BuildingZone, Long> {

    boolean existsByNameIgnoreCase(String name);
}
