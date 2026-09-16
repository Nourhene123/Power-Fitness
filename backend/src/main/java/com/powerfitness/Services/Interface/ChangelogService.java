package com.powerfitness.Services.Interface;

import com.powerfitness.common.domain.PlanContent;
import java.util.List;

/** Diffs two plan snapshots into a plain-language changelog. Port of {@code computeChangelog}. */
public interface ChangelogService {

    List<String> compute(PlanContent oldContent, PlanContent newContent);
}
