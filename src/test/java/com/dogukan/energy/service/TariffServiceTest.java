package com.dogukan.energy.service;

import com.dogukan.energy.entity.Tariff;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.TariffMapper;
import com.dogukan.energy.repository.TariffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private TariffMapper tariffMapper;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void getById_throwsNotFound_whenTariffMissing() {
        when(tariffRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tariffService.getById(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesTariff_whenItExists() {
        Tariff tariff = new Tariff();
        when(tariffRepository.findById(3L)).thenReturn(Optional.of(tariff));

        tariffService.delete(3L);

        verify(tariffRepository).delete(tariff);
    }
}
