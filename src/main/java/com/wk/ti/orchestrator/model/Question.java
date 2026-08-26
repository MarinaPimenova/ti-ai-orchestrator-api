package com.wk.ti.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wk.ti.orchestrator.repository.UUIDStringConverter;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@SuppressWarnings("JpaDataSourceORMInspection")
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "question",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question extends GeneralEntity {
    @Id
    @GeneratedValue(generator = "question_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "question_id_seq", sequenceName = "question_id_seq", allocationSize = 1)
    private Long id; // questionId

    @Convert(converter = UUIDStringConverter.class)
    @Column(name = "conversation_id", columnDefinition = "uuid", nullable = false)
    private String conversationId; // chatId

    @Column(name = "isid")
    private String userId;

    @Column(name = "question")
    private String question;

    @Column(name = "follow_up_question")
    private String followUpQuestion;

    @Column(name = "agent_name_list")
    private String agent;

    @Column(name = "llm_response")
    private String llmResponse;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Type(JsonType.class)
    @Column(name = "source_list", columnDefinition = "jsonb")
    private String source;

    // documentSet
    @Type(JsonType.class)
    @Column(name = "document_list", columnDefinition = "jsonb")
    private String document;

    @Column(name = "status")
    private String status;
}
