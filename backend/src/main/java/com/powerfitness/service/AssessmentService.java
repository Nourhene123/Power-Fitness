package com.powerfitness.service;

import com.powerfitness.dto.AssessmentSubmittedDto;
import com.powerfitness.dto.MyProgramDto;
import com.powerfitness.dto.SubmitAssessmentRequest;
import java.util.Map;

public interface AssessmentService {

    /** Runs the full pipeline: version the assessment, analyze, generate, create the program. */
    AssessmentSubmittedDto submit(Long userId, SubmitAssessmentRequest request);

    boolean hasAssessment(Long userId);

    /** How many assessments (initial + re-dos) the client has submitted — shown on the account page. */
    int assessmentCount(Long userId);

    MyProgramDto currentProgram(Long userId);

    /**
     * The client's latest assessment as a flat field-name → value map — typed columns plus the
     * {@code responses} long tail — used to prefill "answer the coach" fields.
     */
    Map<String, Object> latestAssessmentValues(Long userId);
}
