package com.wk.ti.controller;

import com.wk.ti.orchestrator.model.FeedbackPayload;
import com.wk.ti.orchestrator.model.QuestionPayload;
import com.wk.ti.orchestrator.model.QuestionResponse;
import com.wk.ti.orchestrator.service.QuestionOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@ResponseBody
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionOrchestrator questionOrchestrator;
    private static final String CONVERSATION_NULL_MESSAGE = "conversationId cannot be null";

    // If the AI Assistant is opened directly
    @PostMapping(value = "/question", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionResponse> askQuestion(
            @RequestParam String conversationId,
            @RequestBody QuestionPayload request) {
        Assert.notNull(conversationId, CONVERSATION_NULL_MESSAGE);
        return ResponseEntity.ok(questionOrchestrator.writeQuestionToDatabase(conversationId, request.question()));
    }

    @PostMapping(value = "/feedback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> processUserFeedback(
            @RequestParam String conversationId,
            @RequestBody FeedbackPayload request) {
        Assert.notNull(conversationId, CONVERSATION_NULL_MESSAGE);
        questionOrchestrator.processUserFeedback(conversationId, request);
        return ResponseEntity.ok().build();
    }

/*    @GetMapping(value = "/chats/{chatId}/questions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatQuestionResponse> getChatQuestions(@PathVariable("chatId") String chatId) {
        ChatQuestionResponse response = questionOrchestrator.getChatQuestions(chatId);
        return ResponseEntity.ok(response);
    }*/
}
