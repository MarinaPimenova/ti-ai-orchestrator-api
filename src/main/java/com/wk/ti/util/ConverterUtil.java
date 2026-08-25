package com.wk.ti.util;

import com.wk.ti.agents.Agents;
import com.wk.ti.agents.tool.document.model.*;
import com.wk.ti.orchestrator.model.AgentGeneralEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.wk.ti.util.ParserUtil.parseJsonPayload;

@SuppressWarnings({"unchecked", "unused"})
@Slf4j
public class ConverterUtil {
    private ConverterUtil() {
    }
    /**
     * Converts AgentGeneralEntity for docs/attachments to Optional<AgentResponse>.
     */
    public static Optional<AgentResponse> convertDocsLoggerToAgentResponse(
            Agents agentEnum,
            AgentGeneralEntity agentGeneralEntity,
            String conversationId) {
        if (agentGeneralEntity == null) {
            return Optional.empty();
        }

        List<String> headers = List.of("title", "source");
        List<AgentSearchResult> searchResults = getSearchResults(agentEnum, agentGeneralEntity);
        SourceSet sourceSet = SourceSet.ofDocument(headers, searchResults, agentEnum);

        AgentResponse response = buildAgentResponse(
                conversationId,
                agentGeneralEntity.getQuestionId(),
                agentEnum.name(),
                sourceSet,
                agentGeneralEntity.getSummary(),
                false
        );

        return Optional.of(response);
    }

    /**
     * Returns the relevant search results based on agent type.
     */
    private static List<AgentSearchResult> getSearchResults(Agents agentEnum, AgentGeneralEntity entity) {
/*        if (agentEnum == Agents.DOCUMENT_AGENT) {
            return getAttachmentSourceSet(agentEnum, entity);
        }*/
        return getDocSourceSet(agentEnum, entity);
    }

    /**
     * Parses doc search results from entity rows.
     */
    public static List<AgentSearchResult> getDocSourceSet(Agents agentEnum, AgentGeneralEntity entity) {
        List<AgentSearchResult> rows = parseJsonPayload(entity.getRows(), List.class);
        log.info("Agent[{}] returns documents: {}", agentEnum, rows != null ? rows.size() : 0);
        return rows != null ? rows : Collections.emptyList();
    }


    /**
     * Helper to build AgentResponse consistently.
     */
    private static AgentResponse buildAgentResponse(
            String conversationId,
            Long questionId,
            String agentType,
            SourceSet sourceSet,
            String summary,
            Boolean isHITL) {
        return AgentResponse.builder()
                .conversationId(conversationId)
                .questionId(questionId)
                .termList("")
                .agentType(agentType)
                .sourceSet(sourceSet)
                .summary(summary)
                .documentSet(DocumentSet.builder().build())
                .isHITL(isHITL)
                .build();
    }

    /**
     * Parses rows JSON.
     */
    @SuppressWarnings("unchecked")
    private static List<List<String>> parseRows(String rowsJson) {
        List<List<String>> rows = parseJsonPayload(rowsJson, List.class);
        return rows != null ? rows : Collections.emptyList();
    }

    /**
     * Parses headers JSON.
     */
    @SuppressWarnings("unchecked")
    private static List<String> parseHeaders(String headersJson) {
        List<String> headers = parseJsonPayload(headersJson, List.class);
        return headers != null ? headers : Collections.emptyList();
    }
}
