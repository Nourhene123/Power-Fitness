package com.powerfitness.Entity;

/**
 * State-machine status shared by {@link Program} and {@link ProgramVersion}.
 * draft → in_review → changes_requested → approved → active → superseded → completed → archived
 */
public enum ProgramStatus {
    DRAFT,
    IN_REVIEW,
    CHANGES_REQUESTED,
    APPROVED,
    ACTIVE,
    SUPERSEDED,
    COMPLETED,
    ARCHIVED
}
