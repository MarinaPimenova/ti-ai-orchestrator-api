package com.wk.ti.agents.nodes;

import com.wk.ti.agents.memory.State;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class AggregatorNode implements NodeAction<State> {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are an intelligent response aggregator.

            Your task:
            - Merge multiple agent responses into ONE coherent, natural answer.
            - Remove duplicates and contradictions.
            - Keep the answer concise but informative.
            - If responses cover different aspects, combine them logically.

            Rules:
            - Do NOT mention agent names.
            - Do NOT repeat the same information.
            - Keep a friendly, helpful tone.
            - Prefer structured output if useful (bullet points, short paragraphs).

            If only one response exists, slightly improve clarity but keep meaning unchanged.
            """;

    @Override
    public Map<String, Object> apply(State state) {

        Map<String, String> responses = state.getResponses();

        if (responses == null || responses.isEmpty()) {
            return Map.of(State.CURRENT_MESSAGE_KEY, "No information available.");
        }

        // Build input for LLM
        String combinedInput = responses.entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n\n"));

        String finalResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        Merge the following responses into one coherent answer:

                        %s
                        """.formatted(combinedInput))
                .call()
                .content();

        return Map.of(State.CURRENT_MESSAGE_KEY, finalResponse);
    }
}