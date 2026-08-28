package com.wk.ti.agents.tool.question.sql;

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
public class QuestionSqlTool {

    private final QuestionSqlAgent questionSqlAgent;

    @Tool(description = """
            Use this tool to search and retrieve interview questions from the TI Knowledge Platform database.
            
            Use this tool ONLY when:
            - The user asks about interview questions stored in the Knowledge DB
            - The request is about filtering/searching questions by topic, level, category, project, or resource
            - The user needs structured knowledge-platform question data
            
            Rules:
            - Always use this tool to get question data.
            - Do NOT answer from your own knowledge.
            - Base your response ONLY on the tool result.
            - If no relevant data is found, return the tool response as-is.
            
            Input:
            - The full user question
            
            Output:
            - A concise result based strictly on Knowledge DB question data
            """)
    public String getQuestions(
            String userQuestion,
            String conversationId,
            Long questionId
    ) {
        log.info("Agent [{}] gets parameters: conversationId: {}, questionId: {}",
                Agents.QUESTION_SQL_AGENT.name(), conversationId, questionId);

        AgentResponse agentResponse = questionSqlAgent.getData(conversationId, questionId, userQuestion);

        log.info("Agent [{}] responded successfully.", Agents.QUESTION_SQL_AGENT.name());
        return agentResponse.getSummary();
    }
}