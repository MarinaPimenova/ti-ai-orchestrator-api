package com.wk.ti.controller;

import com.wk.ti.orchestrator.model.CancellationRequest;
import com.wk.ti.orchestrator.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@ResponseBody
@RequestMapping("/rest/v1")
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping(value = "/sse/subscription/{conversationId}/{questionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToSse(@PathVariable("conversationId") String conversationId,
                                     @PathVariable("questionId") Long questionId) {
        return sseService.registerClient(conversationId, questionId);
    }

    @GetMapping(value = "/sse/question", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> answerQuestionAndPostToSse(@RequestParam("conversationId") String conversationId,
                                                           @RequestParam("questionId") Long questionId) {
        Assert.notNull(conversationId, "conversationId cannot be null");
        sseService.startGetAnswerProcessing(conversationId, questionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/sse/subscription/{conversationId}/{questionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cancelSseSubscription(@PathVariable("conversationId") String conversationId,
                                                      @PathVariable("questionId") Long questionId) {
        sseService.cancel(conversationId, questionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/sse/subscriptions/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cancelSubscriptions(@RequestBody CancellationRequest request) {
        sseService.cancelAll(request.subscriptions());
        return ResponseEntity.ok().build();
    }
}

