package com.wk.ti.agents.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wk.ti.agents.Agents;
import com.wk.ti.agents.memory.State;
import com.wk.ti.agents.tool.routing.ClassificationResult;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.tool.ToolCallback;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"removal", "LombokGetterMayBeUsed"})
@Slf4j
public class AgentNode implements NodeAction<State> {

    String agentName;
    String systemPrompt;
    ToolCallback[] tools;
    ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentNode(
            String agentName,
            String systemPrompt,
            ToolCallback[] tools, ChatClient chatClient) {
        this.agentName = agentName;
        this.systemPrompt = systemPrompt;
        this.tools = tools;
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(State state) {

        String userMessage = state.getCurrentMessage();

        ChatClient.ChatClientRequestSpec request = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userMessage);

        if (tools != null && tools.length > 0) {
            request = request.toolCallbacks(tools);
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, String> responses = state.getResponses();

        String response;

        try {
            // -------------------------
            // PROTECTED LLM CALL
            // -------------------------
            response = request.call().content();

        } catch (Exception e) {

            log.error("LLM call failed for agent {}: {}", agentName, e.getMessage());

            response = "AI service is temporarily unavailable due to network issues. Please try again later.";

            result.put("llm_error", true);
        }

        // -------------------------
        // ROUTING AGENT
        // -------------------------
        if (Agents.ROUTING_AGENT.name().equals(agentName)) {
            try {
                ClassificationResult routing =
                        objectMapper.readValue(response, ClassificationResult.class);

                if (routing.getAgents() == null) {
                    throw new RuntimeException("Routing result missing agents");
                }
                Map<String, String> agents = routing.getAgents();
                result.put(State.ROUTING_RESULT_KEY, agents);

            } catch (Exception e) {
                log.error("Routing parsing failed. Raw response: {}", response);

                // fallback
                result.put(State.ROUTING_RESULT_KEY,
                        Map.of("HELLO_AGENT", state.getCurrentMessage()));
            }

        } else {
            result.put(State.CURRENT_MESSAGE_KEY, response);
        }

        result.put(State.PREVIOUS_AGENT_KEY, agentName);

        responses.put(agentName, response);
        result.put(State.RESPONSES_KEY, responses);

        return result;
    }

    public String getAgentName() {
        return agentName;
    }
}
