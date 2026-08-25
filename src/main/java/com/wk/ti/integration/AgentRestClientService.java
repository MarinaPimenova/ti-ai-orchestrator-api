package com.wk.ti.integration;

import com.wk.ti.agents.tool.document.model.AgentResponse;
import com.wk.ti.agents.tool.document.model.DocumentSet;
import com.wk.ti.agents.tool.document.model.SourceSet;
import com.wk.ti.integration.model.AgentPayload;
import com.wk.ti.integration.model.AgentRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.Math.min;
import static java.lang.String.format;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
public abstract class AgentRestClientService implements AgentRestClient {
    public static final String FALLBACK_SUMMARY_TEMPLATE = "%s agent needs more time to process user request: %s";

    private final ExecutorService aiAgentExecutor;
    private final IntegrationRestClient integrationRestClient;

    public AgentResponse fallbackGetData(
            String conversationId,
            Long questionId,
            String question,
            String agent,
            Throwable t) {
        // fallback logic
        log.warn("Action: Failed get data from DB. Parameters: conversationId: {}, questionId: {}, Failed: {}",
                conversationId, questionId, t.getMessage());
        return AgentResponse.builder()
                .conversationId(conversationId)
                .questionId(questionId)
                .agentType(agent)
                .termList(question)
                .summary(format(FALLBACK_SUMMARY_TEMPLATE, agent.toUpperCase(), question))
                .sourceSet(SourceSet.builder().build())
                .documentSet(DocumentSet.fallback())
                .isHITL(false)
                .build();
    }

    public CompletableFuture<AgentResponse> getDataAsync(
            String conversationId, Long questionId, String question, String token) {
        // execute on orchestrator executor to avoid Tomcat thread blocking
        return CompletableFuture.supplyAsync(
                () -> getData(conversationId, questionId, question),
                aiAgentExecutor);
    }

    public AgentResponse getData(String conversationId, Long questionId, String question) {
        AgentResponseSupplierPayload payload = generateAgentResponsePayload(conversationId, questionId, question);
        try {
            return integrationRestClient.executeRequest(
                    payload.url,
                    payload.headers,
                    Optional.of(payload.payload),
                    HttpMethod.POST,
                    AgentResponse.class
            );


        } catch (Exception ex) {
            String message = format("Action: request %s failed. Caused by: %s. Parameters: conversationId: %s, questionId: %d, question: %s",
                    payload.url, ex.getMessage(),
                    conversationId, questionId, getTruncatedQuestion(question));
            log.error(message);
            throw ex;
        }
    }

    protected record AgentResponseSupplierPayload(
            String url,
            HttpHeaders headers,
            AgentPayload payload
    ) {
    }

    protected AgentResponseSupplierPayload generateAgentResponsePayload(
            String conversationId, Long questionId, String question) {
        // use Kubernetes service name (service-b) for intra-cluster calls
        validate(conversationId, questionId, question);

        String url = getAgentUrl(conversationId);
        String truncatedQuestion = getTruncatedQuestion(question);
        log.info("Action: Get data. Parameters: conversationId: {}, questionId: {}, question: {}.",
                conversationId, questionId, truncatedQuestion);
        HttpHeaders headers = integrationRestClient.getHttpHeaders(null);
        AgentPayload payload = new AgentPayload(questionId, question);
        return new AgentResponseSupplierPayload(url, headers, payload);
    }

    private String getTruncatedQuestion(String question) {
        return question.substring(0, min(question.length(), 30));
    }

    // ----------------------------
    // VALIDATION
    // ----------------------------
    private void validate(String conversationId, Long questionId, String question) {
        Assert.notNull(conversationId, "conversationId cannot be null");
        Assert.notNull(questionId, "questionId cannot be null");
        Assert.notNull(question, "question cannot be null");
    }

}
