package com.wk.ti.agents.tool.document;

import com.wk.ti.agents.Agents;
import com.wk.ti.agents.tool.document.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentTool {
    private final DocumentAgent documentAgent;

    @Tool(description = """
            Use this tool to search and retrieve information from documents.
            
            Use this tool ONLY when:
            - The user asks about document content
            - The question requires information from uploaded or indexed documents
            - The request involves analysis, summary, or explanation based on documents
            
            Rules:
            - Always use this tool to get document data.
            - Do NOT answer from your own knowledge.
            - Base your response ONLY on the tool result.
            - If no relevant data is found, return the tool response as-is.
            
            Input:
            - The full user question
            
            Output:
            - A concise summary based strictly on document content
            """)
    public String getDocument(
            String userQuestion,
            String conversationId,
            Long questionId
    ) {
        log.info("Agent [{}] gets parameters: conversationId: {}, questionId: {}", Agents.DOCUMENT_AGENT.name(),
                conversationId, questionId);
        AgentResponse agentResponse = documentAgent.getData(conversationId, questionId, userQuestion);
        log.info("Agent [{}] responded successfully.", Agents.DOCUMENT_AGENT.name());
        return agentResponse.getSummary();
    }

}
