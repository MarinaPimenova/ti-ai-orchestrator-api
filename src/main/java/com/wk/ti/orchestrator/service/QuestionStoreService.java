package com.wk.ti.orchestrator.service;

import com.wk.ti.orchestrator.model.Question;
import com.wk.ti.orchestrator.model.QuestionStatus;
import com.wk.ti.orchestrator.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionStoreService {

    private final QuestionRepository questionRepository;

    protected String getQuestion(long questionId) {
        Question question = questionRepository.findQuestion(questionId);
        return question == null ? null : question.getQuestion();
    }

    @Transactional
    public Long createQuestion(Question newQuestion) {
        return questionRepository.saveAndFlush(newQuestion).getId();
    }

    @Transactional(readOnly = true)
    public Question findQuestion(Long questionId) {
        return questionRepository.findQuestion(questionId);
    }

    /**
     * Updates the stored question with AI response details.
     */
    @Transactional
    public void storeResponse(Question question) {
        questionRepository.save(question);
    }

    @Transactional
    public void processUserFeedback(String feedback, Long questionId) {
        questionRepository.processUserFeedback(feedback, questionId);
    }

    @Transactional
    public void setStatus(Long questionId, String status) {
        if (QuestionStatus.isFinal(status)) {
            questionRepository.setFinalStatus(questionId, status);
        } else {
            questionRepository.setStatus(questionId, status);
        }
    }

    @Transactional
    public void setFallbackStatus(Question question) {
        questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public List<Question> getChatQuestionsForUser(String chatId, String isid) {
        return questionRepository.findAllByConversationIdAndUserIdOrderByCreatedDateAsc(chatId, isid);
    }

}
