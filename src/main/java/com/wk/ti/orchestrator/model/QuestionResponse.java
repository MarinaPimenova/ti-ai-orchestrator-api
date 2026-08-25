package com.wk.ti.orchestrator.model;

public record QuestionResponse(
        String conversationId,
        String questionId,
        String question
) {
}
