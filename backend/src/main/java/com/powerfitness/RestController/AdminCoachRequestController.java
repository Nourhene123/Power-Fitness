package com.powerfitness.RestController;

import com.powerfitness.DTO.CoachRequestDto;
import com.powerfitness.DTO.CoachRequestStatsDto;
import com.powerfitness.DTO.UpdateCoachRequestStatusDto;
import com.powerfitness.Services.Interface.CoachRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coach-requests")
@PreAuthorize("hasAnyRole('COACH','ADMIN')")
public class AdminCoachRequestController {

    private final CoachRequestService service;

    public AdminCoachRequestController(CoachRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<CoachRequestDto> list() {
        return service.findAll();
    }

    @GetMapping("/stats")
    public CoachRequestStatsDto stats() {
        return service.stats();
    }

    @PatchMapping("/{id}")
    public CoachRequestDto updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody UpdateCoachRequestStatusDto body) {
        return service.updateStatus(id, body.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
