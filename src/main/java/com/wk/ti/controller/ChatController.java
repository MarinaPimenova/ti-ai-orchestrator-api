package com.wk.ti.controller;

import com.wk.ti.orchestrator.model.QuestionPayload;
import com.wk.ti.orchestrator.model.RenamePayload;
import com.wk.ti.orchestrator.service.ConversationService;
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
public class ChatController {
    private final ConversationService conversationService;

    private static final String CONVERSATION_NULL_MESSAGE = "conversationId cannot be null";

    // Case#1 User is redirected to AI Assistant from LP HOME
    @PostMapping(value = "/lp/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> newChatAndStoreUserQuestion(@RequestParam String conversationId,
                                                    @RequestBody QuestionPayload request) {
        Assert.notNull(conversationId, CONVERSATION_NULL_MESSAGE);
        Assert.notNull(request, "QuestionPayload must not be null");
        conversationService.newChatAndStoreUserQuestion(conversationId, request);
        return ResponseEntity.ok("Ok");
    }

    @PostMapping(value = "/chat/name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> renameChat(@RequestParam String conversationId,
                                           @RequestBody RenamePayload name) {
        Assert.notNull(conversationId, CONVERSATION_NULL_MESSAGE);
        Assert.notNull(name, "RenamePayload must not be null");
        conversationService.renameChat(conversationId, name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteChat(@RequestParam String conversationId) {
        Assert.notNull(conversationId, CONVERSATION_NULL_MESSAGE);
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.ok().build();
    }

/*    @GetMapping(value = "/chats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatHistoryResponse> getChatHistory(@RequestParam String timezone) {
        notNull(timezone, "timeZone is not provided in the request");
        ChatHistoryResponse response = conversationService.getUserChats(ZoneId.of(timezone));
        return ResponseEntity.ok(response);
    }*/
}
