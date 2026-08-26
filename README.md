# TI AI Orchestrator

AI Orchestrator service for the TI Knowledge Platform.  
Built with **Java 21**, **Spring Boot 4**, **Spring AI**, and **LangGraph4j** to route user requests to specialized agents (document search, SQL question retrieval, greeting/general flow).

---

## Overview

`ti-ai-orchestrator-api` acts as an orchestration layer between a chat client (via gateway) and specialized AI/data agents.

Main responsibilities:

- Route user intent to the correct agent(s)
- Execute agent calls concurrently
- Aggregate multi-agent responses into one coherent answer
- Expose REST/SSE-compatible orchestration behavior
- Integrate with OAuth2/JWT security, PostgreSQL, Redis, OpenAI-compatible endpoint, and observability stack

---

## Architecture (current implementation)

- **Routing Agent**: detects intent and selects agent(s)
- **HELLO Agent**: handles greeting/help/off-topic
- **DOCUMENT Agent**: retrieves and summarizes document-based content
- **QUESTION_SQL Agent**: retrieves interview question data from TI Knowledge DB
- **Dispatcher Node**: executes selected agents in parallel with timeout handling
- **Aggregator Node**: merges agent outputs into a single final response

---

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Security OAuth2 Resource Server (JWT)
- Spring Data JPA (PostgreSQL)
- Spring Data Redis
- Spring AI 2.0.0
- LangGraph4j
- Micrometer + Prometheus
- OpenTelemetry + Zipkin
- Springdoc OpenAPI

---

## Key Configuration

From `application.yml`:

- Server port: `8085` (default)
- OAuth issuer: `https://${OKTA_DOMAIN}/`
- DB: `${ASSISTANT_DB_URL}` / `${ASSISTANT_USER}` / `${ASSISTANT_PASSWORD}`
- Redis: `${MEMORY_PROMPT_REDIS_CACHE_HOST}:${MEMORY_PROMPT_REDIS_CACHE_PORT}`
- OpenAI endpoint: `${OPEN_AI_ENDPOINT}` (default `https://ai-proxy.lab.epam.com`)
- SQL agent endpoint:
    - `${SQL_AGENT_API}` (default `http://localhost:8088`)
    - `api.text-to-sql.search-result=/api/v1/docs?conversationId=<UUID>`
- Document agent endpoint:
    - `${DOCUMENT_AGENT_API}` (default `http://localhost:8086`)
    - `api.document.search-result=/api/v1/docs?conversationId=<UUID>`
    - `api.document.load-url=/api/v1/load-url`
- SSE timeout: 5 min
- Agent timeouts:
    - main: `${ORCHESTRATOR_TIMEOUT_SECONDS:300}`
    - per-agent: `${AGENT_TIMEOUT_SECONDS:180}`

---

## Build & Run

### Prerequisites

- JDK 21
- Gradle (or use wrapper)
- PostgreSQL
- Redis
- Reachable AI endpoint (OpenAI-compatible)

### Run locally

```bash
./gradlew clean build
./gradlew bootRun
```

### Run tests

```bash
./gradlew test
```

---

## API Docs

After startup:

- Swagger UI: `http://localhost:8085/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs`

---

## Package Highlights

- `com.wk.ti.agents`  
  Agent enum and routing ecosystem
- `com.wk.ti.agents.nodes`  
  `DispatcherNode`, `AggregatorNode`
- `com.wk.ti.agents.registry`  
  Agent registration and prompts
- `com.wk.ti.agents.tool.document`  
  Document tool + agent + config
- `com.wk.ti.agents.tool.question.sql`  
  SQL-question tool + agent + config
- `com.wk.ti.integration`  
  External agent REST integration layer
- `com.wk.ti.util`  
  Utility classes (headers/parsing/etc.)

---

## Notes / Known Integration Considerations

1. Keep Spring AI API usage aligned with BOM `2.0.0`.
2. Prefer `ChatClient` + model beans over low-level API construction unless strictly needed.
3. Ensure configured per-agent timeout in YAML is reflected in dispatcher code timeout.
4. Validate external agent contracts for:
    - `conversationId`
    - `questionId`
    - response summary/source payload

---

## Environment Variables (minimum)

- `OKTA_DOMAIN`
- `ASSISTANT_DB_URL`
- `ASSISTANT_USER`
- `ASSISTANT_PASSWORD`
- `MEMORY_PROMPT_REDIS_CACHE_HOST`
- `OPEN_AI_API_KEY`

Recommended:
- `OPEN_AI_ENDPOINT`
- `SQL_AGENT_API`
- `DOCUMENT_AGENT_API`
- `ORCHESTRATOR_TIMEOUT_SECONDS`
- `AGENT_TIMEOUT_SECONDS`

---


