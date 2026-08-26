package com.wk.ti.agents.nodes;

import com.wk.ti.agents.memory.State;
import com.wk.ti.agents.registry.AgentRegistry;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
@RequiredArgsConstructor
@Component
public class DispatcherNode implements NodeAction<State> {

    private final AgentRegistry agentRegistry;

    private final Executor agentExecutor;

    @Override
    public Map<String, Object> apply(State state) {

        Map<String, String> agents = state.getRoutingResult();

        if (agents == null || agents.isEmpty()) {
            return Map.of(State.RESPONSES_KEY, Map.of());
        }

        // Create futures per agent
        List<CompletableFuture<Map.Entry<String, String>>> futures =
                agents.entrySet().stream()
                        .map(entry -> CompletableFuture.supplyAsync(() -> {

                                            String agentName = entry.getKey();
                                            String question = entry.getValue();

                                            try {
                                                var agent = agentRegistry.get(agentName);

                                                // CRITICAL: isolate state per agent
                                                State agentState = new State(new HashMap<>(state.data()));

                                                agentState.value(State.CURRENT_MESSAGE_KEY, question);

                                                Map<String, Object> result = agent.apply(agentState);

                                                String response = (String) result.get(State.CURRENT_MESSAGE_KEY);

                                                return Map.entry(agentName, response);

                                            } catch (Exception e) {
                                                return Map.entry(agentName,
                                                        "Error in " + agentName + ": " + e.getMessage());
                                            }

                                        }, agentExecutor)
                                        .orTimeout(50, TimeUnit.SECONDS)
                                        .exceptionally(ex -> Map.entry(entry.getKey(), "Timeout or error"))
                        )
                        .toList();

        // Wait for all
        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        Map<String, String> responses = allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ))
        ).join();

        return Map.of(State.RESPONSES_KEY, responses);
    }
}