package com.dogukan.energy.service;

import com.dogukan.energy.dto.request.BuildingZoneRequest;
import com.dogukan.energy.entity.BuildingZone;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.BuildingZoneMapper;
import com.dogukan.energy.repository.BuildingZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuildingZoneServiceTest {

    @Mock
    private BuildingZoneRepository zoneRepository;
    @Mock
    private BuildingZoneMapper zoneMapper;

    @InjectMocks
    private BuildingZoneService zoneService;

    @Test
    void create_throwsDuplicate_whenNameAlreadyExists() {
        BuildingZoneRequest request = new BuildingZoneRequest("Production Hall", "desc");
        when(zoneRepository.existsByNameIgnoreCase("Production Hall")).thenReturn(true);

        assertThatThrownBy(() -> zoneService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(zoneRepository, never()).save(any());
    }

    @Test
    void getById_throwsNotFound_whenZoneMissing() {
        when(zoneRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> zoneService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesZone_whenItExists() {
        BuildingZone zone = new BuildingZone();
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

        zoneService.delete(1L);

        verify(zoneRepository).delete(zone);
    }
}
