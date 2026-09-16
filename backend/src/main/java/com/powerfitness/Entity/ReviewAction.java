package com.powerfitness.Entity;

/** Audit-trail action recorded on {@link ProgramReviewEvent}. */
public enum ReviewAction {
    SUBMITTED,
    EDITED,
    APPROVED,
    CHANGES_REQUESTED,
    RESUBMITTED,
    REACTIVATED,
    SUPERSEDED,
    COMPLETED,
    ARCHIVED,
    NEW_VERSION,
    MIGRATED
}
