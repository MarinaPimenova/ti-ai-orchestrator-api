package com.wk.ti.orchestrator.repository;

import com.wk.ti.orchestrator.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** @noinspection SqlResolve, SqlDialectInspection */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
            select *
            from assistant.question cl
            where id= :questionId
            """, nativeQuery = true)
    Question findQuestion(@Param("questionId") Long questionId);

    @Modifying
    @Query(value = """
            UPDATE assistant.question set user_feedback = :userFeedback
            where id = :id
            """, nativeQuery = true)
    void processUserFeedback(@Param(value = "userFeedback") String userFeedback, @Param(value = "id") Long id);

    List<Question> findAllByConversationIdAndUserIdOrderByCreatedDateAsc(String conversationId, String userId);

    @Modifying
    int deleteAllByConversationId(String conversationId);

    @Modifying
    @Query(value = """
            UPDATE assistant.question set status = :status, modified_date = now()
            where id = :id and status not in ('completed', 'failed', 'canceled', 'timed out', 'integration error')
            """, nativeQuery = true)
    void setFinalStatus(@Param(value = "id") Long id, @Param(value = "status") String status);

    // separate method is required to allow user re-ask question, routing logic is on service layer
    @Modifying
    @Query(value = """
            UPDATE assistant.question set status = :status, modified_date = now()
            where id = :id
            """, nativeQuery = true)
    void setStatus(@Param(value = "id") Long id, @Param(value = "status") String status);
}
