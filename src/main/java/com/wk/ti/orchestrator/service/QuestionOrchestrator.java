package com.wk.ti.orchestrator.service;

import com.wk.ti.orchestrator.model.*;
import com.wk.ti.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionOrchestrator {

    private final AIAgentsWorkflow orchestrator;
    private final QuestionService questionService;
    private final ConversationService conversationService;
    private final Executor taskExecutor;

    public void processUserFeedback(String conversationId, FeedbackPayload feedbackPayload) {
        Long questionId = feedbackPayload.getQuestionId();
        log.info("User feedback. ConversationId: {}, QuestionId: {}", conversationId, questionId);
        questionService.processUserFeedback(feedbackPayload.getFeedback(), questionId);
    }

    public AIGenerativeResponse getChatResponse(String conversationId, Long questionId) {
        String userId = SecurityUtil.getCurrentUserIsid();
        String question = questionService.getQuestion(questionId);
        questionService.setStatus(questionId, QuestionStatus.IN_PROGRESS.toString());
        AIGenerativeResponse response = orchestrator.processUserQuestion(conversationId, questionId, userId, question);
        asyncStoreResponse(response);

        return response;
    }

    public QuestionResponse writeQuestionToDatabase(String conversationId, String question) {
        Assert.hasText(question, "Question should not be empty");
        String userId = SecurityUtil.getCurrentUserIsid();
        Optional<Conversation> optionalConversation = conversationService.findConversation(conversationId);
        if (optionalConversation.isEmpty()) {
            log.info("Conversation not found for ID: {}", conversationId);
            optionalConversation = Optional.of(conversationService.createNewChat(conversationId, question));
        }

        Long questionId = questionService.createQuestion(conversationId,
                optionalConversation.get().getId(),
                question, userId);
        conversationService.updateModifiedDate(conversationId);
        return new QuestionResponse(conversationId, questionId.toString(), question);
    }

    protected void updateQuestionStatus(Long questionId, QuestionStatus status) {
        questionService.setStatus(questionId, status.toString());
    }

    /**
     * Stores the AI response asynchronously to avoid blocking the main flow.
     */
    private void asyncStoreResponse(AIGenerativeResponse response) {
        final String followUpQuestion = "";
        CompletableFuture.runAsync(() -> questionService.storeResponse(response, followUpQuestion), taskExecutor)
                .exceptionally(ex -> {
                    log.error("Async store failed for questionId={}: {}", response.getQuestionId(), ex.getMessage(), ex);
                    return null;
                });
    }
}
