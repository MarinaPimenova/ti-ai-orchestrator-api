package com.wk.ti.orchestrator.model;

import java.util.Set;

public enum QuestionStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED,
    TIMED_OUT,
    FAILED,
    INTEGRATION_ERROR;

    /**
     * Converts the enum to a lowercase string with underscores replaced by spaces.
     * Used for database storage (e.g., TIMED_OUT -> "timed out").
     */
    @Override
    public String toString() {
        return name().replace("_", " ").toLowerCase();
    }

    public static boolean isFinal(String status) {
        return Set.of(
            QuestionStatus.COMPLETED.toString(),
            QuestionStatus.CANCELED.toString(),
            QuestionStatus.TIMED_OUT.toString(),
            QuestionStatus.FAILED.toString(),
            QuestionStatus.INTEGRATION_ERROR.toString()
        ).contains(status);
    }
}
