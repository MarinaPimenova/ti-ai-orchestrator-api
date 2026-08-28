package com.wk.ti.agents.tool.question.sql;

import com.wk.ti.agents.Agents;
import com.wk.ti.integration.AgentRestClientService;
import com.wk.ti.integration.IntegrationRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class QuestionSqlAgent extends AgentRestClientService {

    private final QuestionApiConfig questionApiConfig;

    public QuestionSqlAgent(
            ExecutorService aiAgentExecutor,
            IntegrationRestClient integrationRestClient,
            QuestionApiConfig questionApiConfig
    ) {
        super(aiAgentExecutor, integrationRestClient);
        this.questionApiConfig = questionApiConfig;
    }

    @Override
    public Agents getAgent() {
        return Agents.QUESTION_SQL_AGENT;
    }

    @Override
    public String getAgentUrl(String conversationId) {
        return questionApiConfig.getSearchResult().replace("<UUID>", conversationId);
    }
}
