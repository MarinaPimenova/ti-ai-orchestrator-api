package com.wk.ti.integration.model;

import com.wk.ti.agents.Agents;

public interface AgentRestClient {

    Agents getAgent();

    String getAgentUrl(String conversationId);

}
