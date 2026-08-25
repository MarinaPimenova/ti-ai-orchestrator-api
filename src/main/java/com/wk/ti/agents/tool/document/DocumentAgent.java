package com.wk.ti.agents.tool.document;

import com.wk.ti.agents.Agents;
import com.wk.ti.integration.AgentRestClientService;
import com.wk.ti.integration.IntegrationRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class DocumentAgent extends AgentRestClientService {
    private final DocumentApiConfig documentApiConfig;

    public DocumentAgent(
            ExecutorService aiAgentExecutor,
            IntegrationRestClient integrationRestClient,
            DocumentApiConfig documentApiConfig) {
        super(aiAgentExecutor, integrationRestClient);
        this.documentApiConfig = documentApiConfig;
    }

    @Override
    public Agents getAgent() {
        return Agents.DOCUMENT_AGENT;
    }

    @Override
    public String getAgentUrl(String conversationId) {
        return documentApiConfig.getSearchResult().replace("<UUID>", conversationId);
    }
}
