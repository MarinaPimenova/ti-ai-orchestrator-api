package com.wk.ti.orchestrator.service;

import com.wk.ti.orchestrator.model.*;
import com.wk.ti.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wk.ti.util.FileUtil.toTempFileWithUtf8IfText;
import static java.lang.String.format;

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
        Optional<Conversation> conversation = conversationService.findConversation(conversationId);
        if (conversation.isEmpty()) {
            log.info("Conversation not found for ID: {}", conversationId);
            conversationService.createNewChat(conversationId, question);
        }

        Long questionId = questionService.createQuestion(conversationId, question, userId);
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

/*
    public String uploadQuestions(MultipartFile multipartFile) {
        Assert.notNull(multipartFile, "Upload new knowledge context cannot be null");

        try {
            File f = toTempFileWithUtf8IfText(multipartFile);

            FileSystemResource resource = new FileSystemResource(f);

            CompletableFuture<Void> processing = new CompletableFuture<>();
            processing.completeAsync(() -> proxyEvaluatorService.proxyRequestToEvaluator(resource), taskExecutor)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            String message = format("Async processing failed for file=%s. Caused by %s",
                                    multipartFile.getOriginalFilename(), ex.getMessage());
                            log.error(message);
                        } else {
                            String message = format("Async processing completed for file=%s.",
                                    multipartFile.getOriginalFilename());
                            log.info(message);
                        }
                        try {
                            Files.deleteIfExists(f.toPath());
                        } catch (IOException e) {
                            log.warn("Failed to delete temporary file: {}", f.getAbsolutePath(), e);
                        }
                    });
            return f.getName();
        } catch (Exception ex) {
            String message = format("Failed to upload file. caused: %s", ex.getMessage());
            log.error(message);
        }
        return "failed";
    }
*/
}
