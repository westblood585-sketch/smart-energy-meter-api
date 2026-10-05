package com.dogukan.energy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "meters")
public class Meter extends BaseEntity {

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeterStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private BuildingZone zone;

    @OneToMany(mappedBy = "meter", fetch = FetchType.LAZY)
    private List<ConsumptionReading> readings = new ArrayList<>();

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public MeterStatus getStatus() {
        return status;
    }

    public void setStatus(MeterStatus status) {
        this.status = status;
    }

    public BuildingZone getZone() {
        return zone;
    }

    public void setZone(BuildingZone zone) {
        this.zone = zone;
    }

    public List<ConsumptionReading> getReadings() {
        return Collections.unmodifiableList(readings);
    }
}
