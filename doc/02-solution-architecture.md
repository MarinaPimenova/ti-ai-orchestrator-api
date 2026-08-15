# Solution Architecture

## Components

1. AI Chat UI (React)
2. API Gateway
3. AI Orchestrator
4. AI Agents
5. PostgreSQL + pgvector
6. OpenAI

## Architecture Diagram

```text
                 +----------------------+
                 |   React AI Chat UI   |
                 +----------+-----------+
                            |
                    OAuth2 / JWT
                            |
                 +----------v-----------+
                 |    ti-gateway-api    |
                 | Authentication + BFF |
                 +----------+-----------+
                            |
                 REST + SSE Streaming
                            |
          +-----------------v-----------------+
          |       ti-orchestrator-api         |
          |         AI Orchestrator           |
          +----+---------+---------+----------+
               |         |         |
               |         |         |
        +------v-+ +-----v----+ +--v------+
        | SQL    | | Document | | Question|
        | Agent  | | Agent    | | Agent   |
        +----+---+ +-----+----+ +----+----+
             |           |           |
             |           |           |
 PostgreSQL  |      pgvector     PostgreSQL
 Knowledge DB|    Vector Store   Knowledge DB
             |
             +-------------------------+
                                       |
                               +-------v------+
                               | Fallback LLM |
                               | OpenAI GPT   |
                               +--------------+

                       +----------------+
                       | Response Judge |
                       |   Evaluator    |
                       +----------------+
```

## Responsibilities

| Component | Responsibility |
|-----------|----------------|
| React UI | Chat interface, streaming responses |
| Gateway | Authentication, routing |
| AI Orchestrator | Selects appropriate AI agent |
| SQL Agent | Structured DB reasoning |
| Document Agent | RAG retrieval |
| Question Agent | Generate interview content |
| Fallback Agent | General LLM responses |
| Evaluator | Response quality scoring |