package com.dogukan.energy.service;

import com.dogukan.energy.dto.request.MeterRequest;
import com.dogukan.energy.entity.MeterStatus;
import com.dogukan.energy.exception.DuplicateResourceException;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.MeterMapper;
import com.dogukan.energy.repository.BuildingZoneRepository;
import com.dogukan.energy.repository.MeterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock
    private MeterRepository meterRepository;
    @Mock
    private BuildingZoneRepository zoneRepository;
    @Mock
    private MeterMapper meterMapper;

    @InjectMocks
    private MeterService meterService;

    @Test
    void create_throwsDuplicate_whenSerialNumberAlreadyExists() {
        MeterRequest request = new MeterRequest("SM-0001", MeterStatus.ACTIVE, 1L);
        when(meterRepository.existsBySerialNumber("SM-0001")).thenReturn(true);

        assertThatThrownBy(() -> meterService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(zoneRepository, never()).findById(any());
        verify(meterRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenZoneDoesNotExist() {
        MeterRequest request = new MeterRequest("SM-0002", MeterStatus.ACTIVE, 99L);
        when(meterRepository.existsBySerialNumber("SM-0002")).thenReturn(false);
        when(zoneRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> meterService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(meterRepository, never()).save(any());
    }
}
