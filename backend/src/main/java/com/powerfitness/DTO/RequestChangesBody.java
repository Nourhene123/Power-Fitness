package com.powerfitness.DTO;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RequestChangesBody(@NotBlank String note, List<ChangeFieldDto> fields) {}
