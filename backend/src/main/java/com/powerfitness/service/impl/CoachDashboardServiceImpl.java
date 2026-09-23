package com.powerfitness.service.impl;

import com.powerfitness.dto.CoachDashboardDto;
import com.powerfitness.entity.Program;
import com.powerfitness.entity.ProgramStatus;
import com.powerfitness.entity.ProgramVersion;
import com.powerfitness.mapper.CoachDashboardMapper;
import com.powerfitness.repository.ProgramRepository;
import com.powerfitness.repository.ProgramVersionRepository;
import com.powerfitness.service.ClientRiskService;
import com.powerfitness.service.CoachDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoachDashboardServiceImpl implements CoachDashboardService {

    private final ProgramRepository programs;
    private final ProgramVersionRepository versions;
    private final ClientRiskService riskService;
    private final CoachDashboardMapper mapper;

    public CoachDashboardServiceImpl(ProgramRepository programs, ProgramVersionRepository versions,
                                     ClientRiskService riskService, CoachDashboardMapper mapper) {
        this.programs = programs;
        this.versions = versions;
        this.riskService = riskService;
        this.mapper = mapper;
    }

    @Override
    public CoachDashboardDto dashboard() {
        long pendingCount = programs.countByStatus(ProgramStatus.IN_REVIEW);
        long approvedCount = programs.countByStatus(ProgramStatus.ACTIVE);
        long totalClients = programs.countDistinctClients(ProgramStatus.ACTIVE);

        var pending = programs.findByStatusOrderByCreatedAtAsc(ProgramStatus.IN_REVIEW).stream()
                .map(this::toPendingReview)
                .toList();

        var recentlyApproved = versions.findTop10ByStatusOrderByApprovedAtDesc(ProgramStatus.ACTIVE).stream()
                .map(this::toApprovedRoadmap)
                .toList();

        var roster = programs.findByStatusOrderByUser_NameAsc(ProgramStatus.ACTIVE).stream()
                .map(p -> {
                    var risk = riskService.assess(p.getUser());
                    return mapper.toRosterEntry(p, risk.atRisk(), risk.reasons());
                })
                .toList();

        long atRiskCount = roster.stream().filter(CoachDashboardDto.RosterEntry::atRisk).count();

        return new CoachDashboardDto(
                new CoachDashboardDto.Stats(pendingCount, approvedCount, totalClients, atRiskCount),
                pending, recentlyApproved, roster);
    }

    private CoachDashboardDto.PendingReview toPendingReview(Program p) {
        ProgramVersion version = versions.findTopByProgramOrderByVersionNoDesc(p).orElse(null);
        return mapper.toPendingReview(p, version);
    }

    private CoachDashboardDto.ApprovedRoadmap toApprovedRoadmap(ProgramVersion v) {
        return mapper.toApprovedRoadmap(v);
    }
}
