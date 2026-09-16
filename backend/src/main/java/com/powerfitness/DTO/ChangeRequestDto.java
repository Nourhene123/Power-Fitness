package com.powerfitness.DTO;

import java.util.List;
import java.util.Map;

public record ChangeRequestDto(
        Long programId,
        String note,
        List<ChangeFieldDto> fields,
        Map<String, String> prefill) {}
