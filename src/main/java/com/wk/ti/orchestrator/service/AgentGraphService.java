package com.wk.ti.orchestrator.service;

import com.wk.ti.agents.memory.State;

import com.wk.ti.agents.nodes.AggregatorNode;
import com.wk.ti.agents.nodes.DispatcherNode;
import com.wk.ti.agents.registry.AgentRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.wk.ti.agents.Agents.*;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentGraphService {
    private final AgentRegistry agentRegistry;
    private final DispatcherNode dispatcherNode;
    private final AggregatorNode aggregatorNode;

    public StateGraph<State> getQuestionWorkflow() {

        String routingAgent = ROUTING_AGENT.name();
        String dispatcher = "DISPATCHER";
        String aggregator = "AGGREGATOR";

        StateGraph<State> graph;

        try {
            graph = new StateGraph<>(State.SCHEMA, State::new)
                    .addNode(routingAgent, AsyncNodeAction.node_async(agentRegistry.get(routingAgent)))
                    .addNode(dispatcher, AsyncNodeAction.node_async(dispatcherNode))
                    .addNode(aggregator, AsyncNodeAction.node_async(aggregatorNode));

        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }

        try {
            graph.addEdge(START, routingAgent);

            // ROUTER → DISPATCHER (always)
            graph.addConditionalEdges(
                    routingAgent,
                    state -> CompletableFuture.completedFuture(dispatcher),
                    Map.of(dispatcher, dispatcher)
            );

            // DISPATCHER → AGGREGATOR
            graph.addEdge(dispatcher, aggregator);

            // AGGREGATOR → END
            graph.addEdge(aggregator, END);

        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }

        return graph;
    }
}
