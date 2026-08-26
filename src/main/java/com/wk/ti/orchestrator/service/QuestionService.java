package com.wk.ti.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wk.ti.orchestrator.model.AIGenerativeResponse;
import com.wk.ti.orchestrator.model.Question;
import com.wk.ti.orchestrator.model.QuestionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {
    private final ObjectMapper mapper;
    private final QuestionStoreService questionStoreService;

    protected String getQuestion(long questionId) {
        return questionStoreService.getQuestion(questionId);
    }

    /**
     * Persist the question and return its generated ID.
     */
    
    public Long createQuestion(String conversationId, String question, String userId) {
        Question newQuestion = Question.builder()
                .conversationId(conversationId)
                .userId(userId)
                .question(question)
                .status(QuestionStatus.CREATED.toString())
                .build();

        return questionStoreService.createQuestion(newQuestion);
    }

    /**
     * Updates the stored question with AI response details.
     */
    public void storeResponse(AIGenerativeResponse response, String followUpQuestion) {
        Question question = questionStoreService.findQuestion(response.getQuestionId());
        if (question == null) {
            log.warn("Can't store response. No question found for ID: {}", response.getQuestionId());
            return;
        }

        question.setLlmResponse(response.getContent());
        question.setModifiedDate(Instant.now());
        //question.setAgent(String.join(", ", response.getAgents()));
        // set follow-up question
        //question.setFollowUpQuestion(followUpQuestion);

        questionStoreService.storeResponse(question);
    }


    public void processUserFeedback(String feedback, Long questionId) {
        questionStoreService.processUserFeedback(feedback, questionId);
    }

    public void setStatus(Long questionId, String status) {
        questionStoreService.setStatus(questionId, status);
    }

/*
    public void setFallbackStatus(Long questionId, Set<Agents> usedAgents) {
        Question question = questionStoreService.findQuestion(questionId);
        if (question == null) {
            log.warn("Can't set fallback status. No question found for ID: {}", questionId);
            return;
        }

        String agents = String.join(", ", usedAgents.stream().map(Enum::name).toList());
        question.setAgent(agents);
        question.setStatus(QuestionStatus.FAILED.toString());
        question.setModifiedDate(Instant.now());
        questionStoreService.setFallbackStatus(question);
    }

    protected ChatQuestionResponse getChatQuestionsForUser(String chatId, String isid) {
        List<Question> chatQuestions =
                questionStoreService.getChatQuestionsForUser(chatId, isid);
        List<QuestionWithFeedback> userQuestions = chatQuestions.stream().map(question ->
                new QuestionWithFeedback(
                        question.getConversationId(),
                        question.getId(),
                        question.getQuestion(),
                        question.getFollowUpQuestion(),
                        question.getAgent(),
                        question.getSource() == null ? null : parseJsonPayload(question.getSource(), SourceSet.class),
                        question.getDocument() == null ? null : parseJsonPayload(question.getDocument(), DocumentSet.class),
                        question.getLlmResponse() == null ? "" : question.getLlmResponse(),
                        question.getUserFeedback(),
                        question.getStatus(),
                        String.valueOf(question.getCreatedDate().toEpochMilli())
                )
        ).toList();
        return new ChatQuestionResponse(userQuestions);
    }
*/
}
