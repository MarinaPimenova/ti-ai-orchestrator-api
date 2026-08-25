package com.wk.ti.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.wk.ti.orchestrator.repository.UUIDStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@SuppressWarnings("JpaDataSourceORMInspection")
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "chat",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Conversation extends GeneralEntity {
    @Id
    @GeneratedValue(generator = "chat_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "chat_id_seq_generator", sequenceName = "chat_id_seq", allocationSize = 1)
    private Long id;

    @Convert(converter = UUIDStringConverter.class)
    @Column(name = "conversation_id", columnDefinition = "uuid", nullable = false)
    private String conversationId; // Global chat ID

    @Column(name = "user_id")
    private String userId;

    @Column(name = "start_question")
    private String startQuestion;

    @Column(name = "chat_name")
    private String chatName;
}
