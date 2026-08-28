package com.wk.ti.orchestrator.service;

import com.wk.ti.orchestrator.model.Conversation;
import com.wk.ti.orchestrator.model.QuestionPayload;
import com.wk.ti.orchestrator.model.RenamePayload;
import com.wk.ti.orchestrator.repository.ConversationRepository;
import com.wk.ti.orchestrator.repository.QuestionRepository;
import com.wk.ti.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

import static com.wk.ti.util.StringUtil.generateChatName;

@SuppressWarnings("unused")
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationService {
    private static final int RETRY_DELAY_MS = 100;
    private final ConversationRepository conversationRepository;
    private final QuestionRepository questionRepository;
    @Value("${app.chat-history.days}") private int chatHistoryDaysCount;

    /**
     * Case#1: User is redirected to AI Assistant from LP HOME.
     */
    public void newChatAndStoreUserQuestion(String conversationId, QuestionPayload questionPayload) {
        createNewChat(conversationId, questionPayload.question());
    }

    /**
     * Creates and persists a new conversation.
     */
    protected Conversation createNewChat(String conversationId, String question) {
        var conversation = Conversation.builder()
                .conversationId(conversationId)
                .startQuestion(question)
                .chatName(generateChatName(question))
                .userId(SecurityUtil.getCurrentUserIsid())
                .build();

        var savedConversation = conversationRepository.saveAndFlush(conversation);
        log.info("New chat created. Conversation ID: {}, Database ID: {}",
                savedConversation.getConversationId(), savedConversation.getId());
        return savedConversation;
    }

    /**
     * Retrieves the start question for a given conversation ID.
     */
    protected String getStartQuestion(String conversationId) {
        Optional<Conversation> conversation = findConversationWithRetry(conversationId);
        return conversation.isPresent() ? conversation.get().getStartQuestion() : "";
    }

    
    public void renameChat(String conversationId, RenamePayload name) {
        Assert.hasText(name.newName(), "New chat name must not be null or empty");
        Assert.isTrue(name.newName().length() <= 50, "New chat name must not exceed 50 characters");
        findConversationOrThrowException(conversationId);
        int rowsChanged = conversationRepository.updateChatName(name.newName(), UUID.fromString(conversationId));
        if (rowsChanged == 0) {
            log.warn("Conversation with ID: {} failed to rename", conversationId);
            throw new RuntimeException("Failed to rename conversation for ID: " + conversationId);
        }
    }

/*
    @Transactional(readOnly = true)
    public ChatHistoryResponse getUserChats(ZoneId userZoneId) {
        String userId = SecurityUtil.getCurrentUserIsid();
        Instant historyStart = getStartChatHistoryDate(userZoneId, chatHistoryDaysCount);
        List<Conversation> conversations =
                conversationRepository.findAllByUserIdBeforeOrEqualModifiedDate(userId, historyStart);
        List<ChatHistoryItem> chatHistoryItems = conversations.stream()
                .map(conversation -> {
                    Instant modifiedAt = conversation.getModifiedDate();
                        return ChatHistoryItem.builder()
                                .chatId(UUID.fromString(conversation.getConversationId()))
                                .chatName(conversation.getChatName())
                                .userTzCreatedDate(getUserDate(modifiedAt, userZoneId))
                                .userTzGeneralDate(getDateAlias(modifiedAt, userZoneId))
                                .utcTimestamp(String.valueOf(modifiedAt.toEpochMilli()))
                                .build();
                })
                .toList();

        return ChatHistoryResponse.builder()
                .chats(chatHistoryItems)
                .historyDaysCount(chatHistoryDaysCount)
                .build();
    }
*/

    /**
     * Retry logic for transient conversation lookups.
     */
    private Optional<Conversation> findConversationWithRetry(String conversationId) {
        UUID conversationUUID = UUID.fromString(conversationId);
        Optional<Conversation> conversation = conversationRepository.findConversation(conversationUUID);
        if (conversation.isPresent()) {
            return conversation;
        }

        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Retry sleep interrupted for conversationId={}", conversationId, e);
            return Optional.empty();
        }

        return conversationRepository.findConversation(conversationUUID);
    }

    protected Optional<Conversation> findConversation(String conversationId) {
        return conversationRepository.findConversation(UUID.fromString(conversationId));
    }

    @Transactional
    public void updateModifiedDate(String conversationId) {
        int rowsChanged = conversationRepository.updateModifiedDate(UUID.fromString(conversationId));
        if (rowsChanged == 0) {
            log.warn("No conversation found to update modified date for ID: {}", conversationId);
        }
    }

    
    @Transactional
    public void deleteConversation(String conversationId) {
        Conversation conversation = findConversationOrThrowException(conversationId);
        try {
            int rowsDeleted = questionRepository.deleteAllByConversationId(conversationId);
            if (rowsDeleted > 0) {
                log.info("Deleted {} questions for conversationId: {}", rowsDeleted, conversationId);
            } else {
                log.warn("No questions found to delete for conversationId: {}", conversationId);
            }
            conversationRepository.deleteById(conversation.getId());

        } catch (DataAccessException e) {
            log.error("Failed to delete conversation and its questions for conversationId: {}", conversationId, e);
            throw new RuntimeException("Failed to delete the conversation for ID: " + conversationId, e);
        }
    }

    protected Conversation findConversationOrThrowException(String conversationId) {
        Assert.notNull(conversationId, "conversationId cannot be null");
        Optional<Conversation> conversationOpt = conversationRepository.findConversation(UUID.fromString(conversationId));
        if (conversationOpt.isEmpty()) {
            log.warn("No conversation found for ID: {}", conversationId);
            throw new RuntimeException(conversationId);
        }
        return conversationOpt.get();
    }
}
