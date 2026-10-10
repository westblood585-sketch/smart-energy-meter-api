package com.dogukan.energy.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dogukan.energy.dto.request.TariffRequest;
import com.dogukan.energy.dto.response.TariffResponse;
import com.dogukan.energy.entity.Tariff;
import com.dogukan.energy.exception.ResourceNotFoundException;
import com.dogukan.energy.mapper.TariffMapper;
import com.dogukan.energy.repository.TariffRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TariffService {
    private static final Logger log = LoggerFactory.getLogger(TariffService.class);


    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;

    public TariffService(TariffRepository tariffRepository, TariffMapper tariffMapper) {
        this.tariffRepository = tariffRepository;
        this.tariffMapper = tariffMapper;
    }

    @Transactional
    public TariffResponse create(TariffRequest request) {
        log.info("Executing TariffService#create");
        Tariff saved = tariffRepository.save(tariffMapper.toEntity(request));
        return tariffMapper.toResponse(saved);
    }

    @Transactional
    public TariffResponse update(Long id, TariffRequest request) {
        log.info("Executing TariffService#update");
        Tariff tariff = findEntity(id);
        tariffMapper.updateEntity(request, tariff);
        return tariffMapper.toResponse(tariff);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Executing TariffService#delete");
        tariffRepository.delete(findEntity(id));
    }

    public TariffResponse getById(Long id) {
        log.info("Executing TariffService#getById");
        return tariffMapper.toResponse(findEntity(id));
    }

    public Page<TariffResponse> getAll(Pageable pageable) {
        log.info("Executing TariffService#getAll");
        return tariffRepository.findAll(pageable).map(tariffMapper::toResponse);
    }

    private Tariff findEntity(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff", id));
    }
}
