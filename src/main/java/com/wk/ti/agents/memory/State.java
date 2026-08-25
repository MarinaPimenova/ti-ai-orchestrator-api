package com.wk.ti.agents.memory;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;


import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("removal")
public class State extends AgentState {
    public static final String PREVIOUS_AGENT_KEY = "current_agent";
    public static final String CURRENT_MESSAGE_KEY = "current_message";
    public static final String ROUTING_RESULT_KEY = "routing_result";
    public static final String RESPONSES_KEY = "responses";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String QUESTION_ID = "questionId";

    public State(Map<String, Object> initData) {
        super(initData);
    }

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            CONVERSATION_ID, Channels.base(() -> ""),
            QUESTION_ID, Channels.base(() -> 0L),
            PREVIOUS_AGENT_KEY, Channels.base(() -> ""),
            CURRENT_MESSAGE_KEY, Channels.base(() -> ""),
            ROUTING_RESULT_KEY, Channels.base(() -> new HashMap<>()),
            RESPONSES_KEY, Channels.base(() -> new HashMap<>())
    );

    public String getPreviousAgentKey() {
        return this.<String>value(PREVIOUS_AGENT_KEY).orElse("NO AGENT");
    }

    public String getCurrentMessage() {
        return this.<String>value(CURRENT_MESSAGE_KEY).orElse("");
    }

    public void setRoutingResult(Map<String, String> result) {
        this.value(ROUTING_RESULT_KEY, result);
    }

    public Map<String, String> getRoutingResult() {
        return this.<Map<String, String>>value(ROUTING_RESULT_KEY)
                .orElse(new HashMap<>());
    }

    public Map<String, String> getResponses() {
        return this.<Map<String, String>>value(RESPONSES_KEY)
                .orElse(new HashMap<>());
    }

    public String getConversationId() {
        return this.<String>value(CONVERSATION_ID).orElse("");
    }

    public Long getQuestionId() {
        return this.<Long>value(QUESTION_ID).orElse(0L);
    }
}
