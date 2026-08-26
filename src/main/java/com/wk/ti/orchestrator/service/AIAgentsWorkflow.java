package com.wk.ti.orchestrator.service;

import com.wk.ti.agents.memory.State;

import com.wk.ti.orchestrator.model.AIGenerativeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Service
@Slf4j
@RequiredArgsConstructor
public class AIAgentsWorkflow {
    private final AgentGraphService agentGraphService;

    public AIGenerativeResponse processUserQuestion(String conversationId, Long questionId, String userId, String question) {
        Assert.notNull(conversationId, "conversationId cannot be null");
        Assert.notNull(questionId, "questionId cannot be null");
        Assert.notNull(question, "question cannot be null");

        log.info("conversationId: {}, questionId: {}, user {}, Question: {}", conversationId,
                questionId, userId, question);
        StateGraph<State> stateGraph = agentGraphService.getQuestionWorkflow();
        CompiledGraph<State> compiledGraph;
        try {
            compiledGraph = stateGraph.compile();
        } catch (GraphStateException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        // set up conversationId & questionId for DOCUMENT_AGENT

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("HUMAN INPUT", ""));

        Optional<State> optState = compiledGraph.invoke(Map.of(
                State.CONVERSATION_ID, conversationId,
                State.QUESTION_ID, questionId,
                State.CURRENT_MESSAGE_KEY, question,
                State.PREVIOUS_AGENT_KEY, "HUMAN INPUT",
                State.ROUTING_RESULT_KEY, new HashMap<>()
        ));
        String finalResponse = "";
        if (optState.isPresent()) {
            finalResponse = optState.get().getCurrentMessage();
        }
        return AIGenerativeResponse.builder()
                .content(finalResponse)
                .questionId(questionId)
                .sessionId(conversationId)
                .build();
    }

}
