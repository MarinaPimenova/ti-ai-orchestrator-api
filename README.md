# TI AI Orchestrator

AI Orchestrator service for the TI Knowledge Platform.
Built with **Java 21**, **Spring Boot 4**, **Spring AI**, and **LangGraph4j** to route user requests to specialized agents (document search, SQL question retrieval, greeting/general flow).

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture-current-implementation)
- [Tech Stack](#tech-stack)
- [Configuration](#configuration)
- [Package Highlights](#package-highlights)
- [Build & Run](#build--run)
- [Run Locally & Test Functionality](#run-locally--test-functionality)
- [API Docs](#api-docs)
- [Notes / Known Integration Considerations](#notes--known-integration-considerations)

---

## Overview

`ti-ai-orchestrator-api` acts as an orchestration layer between a chat client (via gateway) and specialized AI/data agents.

Main responsibilities:

- Route user intent to the correct agent(s)
- Execute agent calls concurrently
- Aggregate multi-agent responses into one coherent answer
- Expose REST/SSE-compatible orchestration behavior
- Integrate with OAuth2/JWT security (gateway-facing endpoints), PostgreSQL, Redis, an OpenAI-compatible endpoint, and an observability stack

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
- Spring AI 2.0.1
- LangGraph4j
- Micrometer + Prometheus
- OpenTelemetry + Zipkin
- Springdoc OpenAPI

---

## Configuration

All settings below come from `src/main/resources/application.yaml` (defaults shown are what applies if the corresponding env var is unset). `src/main/resources/application-local.yaml` overrides a subset of these for local development (see [Run Locally & Test Functionality](#run-locally--test-functionality)).

### Server

| Setting | Property | Default |
|---|---|---|
| Port | `server.port` | `${SERVER_PORT:8085}` |
| Session timeout | `server.servlet.session.timeout` | `3600` (1h) |

### Security

- OAuth issuer: `https://${OKTA_DOMAIN}/` (`spring.security.oauth2.resourceserver.jwt.issuer-uri`)
- Enforced by `com.wk.ti.config.ResourceServerConfig`, which explicitly `permitAll()`s `/rest/**` (all business endpoints in this service live under `/rest/v1`), `/v3/**`, `/swagger-ui/**`, and `/actuator/**`. Only paths under `/api/**` and `/` require a valid JWT. In practice, this service can be exercised with **no token at all**.

### Database (PostgreSQL / `assistant` schema)

- URL / credentials: `${ASSISTANT_DB_URL}` / `${ASSISTANT_USER}` / `${ASSISTANT_PASSWORD}`
- Hibernate `ddl-auto: none` (schema is pre-provisioned, not generated)

### Redis

- Conversation/prompt-memory cache: `${MEMORY_PROMPT_REDIS_CACHE_HOST}:${MEMORY_PROMPT_REDIS_CACHE_PORT:6380}`

### OpenAI-compatible chat model

- Endpoint: `${OPEN_AI_ENDPOINT:https://ai-proxy.lab.epam.com}`
- API key: `${OPEN_AI_API_KEY}`
- Completions path: `${OPEN_AI_COMPLETIONS_PATH:/openai/deployments/gpt-4.1-mini-2025-04-14/chat/completions}`
- Model: `${CHAT_MODEL:gpt-4.1-mini-2025-04-14}`

### Downstream agents

- SQL agent: `${SQL_AGENT_API:http://localhost:8088}`, results read from `/rest/v1/docs?conversationId=<UUID>`
- Document agent: `${DOCUMENT_AGENT_API:http://localhost:8087}`, results read from `/rest/v1/docs?conversationId=<UUID>`, document loading via `/api/v1/load-url`

### Timeouts

- SSE connection timeout: 5 min (`app.sse.timeout`)
- Orchestrator (main) timeout: `${ORCHESTRATOR_TIMEOUT_SECONDS:300}` seconds
- Per-agent timeout: `${AGENT_TIMEOUT_SECONDS:180}` seconds

### Environment variables (minimum to boot against real infra)

- `OKTA_DOMAIN`
- `ASSISTANT_DB_URL`, `ASSISTANT_USER`, `ASSISTANT_PASSWORD`
- `MEMORY_PROMPT_REDIS_CACHE_HOST`
- `OPEN_AI_API_KEY`

Recommended:

- `OPEN_AI_ENDPOINT`
- `SQL_AGENT_API`, `DOCUMENT_AGENT_API`
- `ORCHESTRATOR_TIMEOUT_SECONDS`, `AGENT_TIMEOUT_SECONDS`

> For a fully standalone local run (no Okta, no shared infra), use the `local` Spring profile instead - see below.

---

## Package Highlights

- `com.wk.ti.controller`
  REST controllers (`ChatController`, `QuestionController`, `SseController`, `VersionController`)
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
- `com.wk.ti.orchestrator`
  Conversation/question domain, orchestration services, SSE service
- `com.wk.ti.integration`
  External agent REST integration layer
- `com.wk.ti.util`
  Utility classes (headers/parsing/etc.)

---

## Build & Run

### Prerequisites

- JDK 21
- Gradle (or use the wrapper)
- PostgreSQL
- Redis
- Reachable AI endpoint (OpenAI-compatible)

### Build & unit test

```bash
./gradlew clean build
./gradlew test
```

### Run (against real/shared infra)

```bash
./gradlew bootRun
```

---

## Run Locally & Test Functionality

Complete guide to running **just this microservice** standalone - with its own database and cache in Docker - and exercising its main scenarios via the provided `.http` files, without any Okta/OAuth setup.

### Prerequisites

- Docker (and Docker Compose)
- JDK 21
- An `OPEN_AI_API_KEY` for the OpenAI-compatible proxy (`application-local.yaml` ships a working default key for the `ai-proxy.lab.epam.com` dev proxy - override it if it has expired or you have your own)
- An HTTP client that understands `.http` files (IntelliJ IDEA's built-in HTTP Client, or the JetBrains HTTP Client CLI)

### Step 1 - Start the minimum required infrastructure

This service only needs its own Postgres database and its own Redis cache to run standalone - it does **not** need the rest of the TI platform (gateway, knowledge DB, RabbitMQ, etc.). Per-service compose files for exactly that live in the sibling `ti-gateway-api/docker` folder (commands below assume `ti-gateway-api` is checked out next to this repo, e.g. both under `ti-2026/`):

```bash
cd ../ti-gateway-api/docker
docker compose -f _05_assistant_postgres.yaml -f _02_memory-prompt-redis-service.yaml up -d
```

This starts:

| Service | Container | Host port | Matches `application-local.yaml` |
|---|---|---|---|
| PostgreSQL (`assistant_db`) | `ti-assistant-db` | `5434` | `jdbc:postgresql://localhost:5434/assistant_db` |
| Redis (prompt/memory cache) | `ti-memory-prompt-redis` | `6380` | `127.0.0.1:6380` |

Optional, only if you want tracing/metrics locally (both disabled by default in the `local` profile):

```bash
docker compose -f _08_zipkin.yaml up -d   # Zipkin UI: http://localhost:9411
docker compose -f _06_prometheus.yaml up -d
```

> `ti-sql-agent` (port 8088) and `ti-document-agent` (port 8087) are separate microservices consumed by this orchestrator. They are **not required** to run this service or its HELLO_AGENT flow, but the DOCUMENT_AGENT / QUESTION_SQL_AGENT scenarios below will fail/timeout while calling out to them if they are not also running (see [`docker-compose-full.yml`](../ti-gateway-api/docker/docker-compose-full.yml) for how to start them too).

### Step 2 - Run the application with the `local` profile

The `local` profile (`application-local.yaml`) already points at the containers above, uses a fixed dev OAuth issuer (no `OKTA_DOMAIN` needed), and disables tracing/most metrics:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Or, from IntelliJ IDEA, run `com.wk.ti.Application` with the active profile set to `local` (Run Configuration → Active profiles: `local`).

The app starts on `http://localhost:8085`.

### Step 3 - Sanity check

```http
GET http://localhost:8085/rest/v1/version
```

(`http/version.http`), or check `http/actuator/actuator.http` → `GET /actuator/health` to confirm the app, Postgres and Redis are all wired up (`status: UP`).

### Step 4 - Exercise the main scenarios via `.http` files

All files live under [`http/`](http) and target the `dev` environment declared in `http/http-client.env.json` (`host = http://localhost:8085`). None of these require an `Authorization` header - `/rest/**` is `permitAll`.

| File | Covers |
|---|---|
| [`http/version.http`](http/version.http) | `GET /rest/v1/version` - quick liveness check |
| [`http/actuator/actuator.http`](http/actuator/actuator.http) | `/actuator/health`, `/actuator/info`, `/actuator/prometheus` (the latter is disabled under the `local` profile, see file notes) |
| [`http/chat/chat.http`](http/chat/chat.http) | Chat lifecycle: create a chat from the Landing Page entry point (`POST /lp/chat`), rename it (`POST /chat/name`), delete it (`DELETE /chat`) |
| [`http/question/question.http`](http/question/question.http) | Ask a question inside an existing/ongoing chat (`POST /question`), with three sample payloads tuned to route to HELLO_AGENT, DOCUMENT_AGENT and QUESTION_SQL_AGENT respectively; submit feedback on an answer (`POST /feedback`) |
| [`http/sse/sse.http`](http/sse/sse.http) | Subscribe to the SSE stream for a question, trigger the async answer pipeline, cancel a subscription (single and batch) |
| [`http/open-ai/openai.http`](http/open-ai/openai.http) | Direct call to the OpenAI-compatible proxy, bypassing the orchestrator - useful to isolate model/API-key issues |

Suggested end-to-end run order (these files pass `conversationId`/`questionId` between each other via IntelliJ HTTP Client global variables, set in response handler scripts):

1. `http/question/question.http` → run request **1** ("Ask a question routed to HELLO_AGENT") - this is the safest end-to-end path since it needs no downstream agent service.
2. `http/sse/sse.http` → run request **1** (subscribe, leave the streaming response open), then request **2** (trigger processing) in a second tab/terminal - the aggregated answer arrives on the open SSE connection.
3. `http/question/question.http` → run request **4** (submit feedback) for the same question.
4. `http/chat/chat.http` → exercise chat rename/delete independently (self-contained, generates its own `conversationId`).

To also exercise the DOCUMENT_AGENT and QUESTION_SQL_AGENT routing (requests **2** and **3** in `question.http`), additionally start `ti-document-agent` and `ti-sql-agent` (with their own dependent DBs) before triggering the SSE processing step.

---

## API Docs

After startup:

- Swagger UI: `http://localhost:8085/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs`

---

## Notes / Known Integration Considerations

1. Keep Spring AI API usage aligned with BOM `2.0.0`.
2. Prefer `ChatClient` + model beans over low-level API construction unless strictly needed.
3. Ensure the configured per-agent timeout in YAML is reflected in dispatcher code timeout.
4. Validate external agent contracts for:
    - `conversationId`
    - `questionId`
    - response summary/source payload
