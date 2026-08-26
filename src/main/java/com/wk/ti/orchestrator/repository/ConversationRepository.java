package com.wk.ti.orchestrator.repository;

import com.wk.ti.orchestrator.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @noinspection SqlResolve, SqlDialectInspection
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query(value = """
            select *
            from assistant.chat cl
            where cl.conversation_id = :conversationId
            """, nativeQuery = true)
    Optional<Conversation> findConversation(@Param("conversationId") UUID conversationId);


    @Transactional
    @Modifying
    @Query(value = """
            update assistant.chat
            set chat_name = :chatName
            where conversation_id = :conversationId
            """, nativeQuery = true)
    int updateChatName(@Param("chatName") String chatName,
                       @Param("conversationId") UUID conversationId);

    @Modifying
    @Query(value = """
            update assistant.chat
            set modified_date = now()
            where conversation_id = :conversationId
            """, nativeQuery = true)
    int updateModifiedDate(@Param("conversationId") UUID conversationId);

    @Query(value = """
            select *
            from assistant.chat cl
            where cl.user_id = :userId
              and cl.modified_date >= :historyTs
            order by cl.modified_date desc
            """, nativeQuery = true)
    List<Conversation> findAllByUserIdBeforeOrEqualModifiedDate(
            @Param("userId") String userIsid,
            @Param("historyTs") Instant historyTs);
}
