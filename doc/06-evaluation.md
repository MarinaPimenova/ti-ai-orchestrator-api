# AI Response Evaluation

## Goal

Measure chatbot quality before production deployment.

## Evaluator

The evaluator reviews every response using another LLM.

Evaluation criteria:

| Metric | Description |
|---------|-------------|
| Groundedness | Uses retrieved context |
| Correctness | Technically accurate |
| Completeness | Fully answers question |
| Relevance | Matches user intent |

Score range:

- 1 (poor)
- 5 (excellent)

## Automated Flow

```text
User Question
      │
      ▼
 AI Orchestrator
      │
      ▼
 Selected Agent
      │
      ▼
 Generated Answer
      │
      ├─────────────► User
      │
      ▼
 Evaluator
      │
      ▼
 Score + Feedback
      │
      ▼
 Prometheus Metrics
```

## Success Criteria

| KPI | Target |
|------|--------|
| Grounded answers | ≥90% |
| Overall correctness | ≥85% |
| SQL accuracy | ≥95% |
| RAG response time | {"<"}3 sec |
| Generated question acceptance | ≥75% |

## Metrics

Example Micrometer metrics:

- chatbot.requests.total
- chatbot.response.duration
- chatbot.agent.sql.count
- chatbot.agent.document.count
- chatbot.agent.generator.count
- chatbot.agent.fallback.count
- chatbot.evaluator.score