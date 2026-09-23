package com.powerfitness.service.impl;

import com.powerfitness.dto.CoachRequestDto;
import com.powerfitness.dto.CoachRequestStatsDto;
import com.powerfitness.dto.CreateCoachRequestDto;
import com.powerfitness.entity.CoachRequest;
import com.powerfitness.exception.ResourceNotFoundException;
import com.powerfitness.mapper.CoachRequestMapper;
import com.powerfitness.repository.CoachRequestRepository;
import com.powerfitness.service.CoachRequestService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CoachRequestServiceImpl implements CoachRequestService {

    private final CoachRequestRepository repository;
    private final CoachRequestMapper mapper;

    public CoachRequestServiceImpl(CoachRequestRepository repository, CoachRequestMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CoachRequestDto submit(CreateCoachRequestDto request) {
        CoachRequest saved = repository.save(mapper.toEntity(request));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachRequestDto> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachRequestStatsDto stats() {
        return new CoachRequestStatsDto(
                repository.count(),
                repository.countByStatus("pending"),
                repository.countByStatus("contacted"),
                repository.countByStatus("completed"));
    }

    @Override
    public CoachRequestDto updateStatus(Long id, String status) {
        CoachRequest entity = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Coach request", id));
        entity.setStatus(status);
        return mapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.of("Coach request", id);
        }
        repository.deleteById(id);
    }
}
