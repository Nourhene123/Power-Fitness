package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.ClientProfileDto;
import com.powerfitness.DTO.ClientProgressDto;
import com.powerfitness.DTO.ExerciseSummaryDto;
import java.util.List;

/** Coach-facing reads about a single client. Port of {@code review_roadmap.php} + {@code client_progress.php}. */
public interface CoachClientService {

    ClientProfileDto profile(Long userId);

    ClientProgressDto progress(Long userId);

    /** The exercise library filtered by the client's injuries/equipment — for the plan editor's autocomplete. */
    List<ExerciseSummaryDto> safeExercisesFor(Long userId);
}
