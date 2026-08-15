# AI Agents

The AI Orchestrator follows an Agentic AI approach where each agent has a single responsibility.

## SQL Agent

### Purpose

Answers questions using structured knowledge from PostgreSQL.

### Examples

- List all Spring Boot questions.
- Show senior Java questions.
- Find Docker questions for Project X.

### Implementation

- LangChain4j SQL Tool
- Spring Data JPA
- PostgreSQL

The LLM generates SQL only over whitelisted tables.

---

## Document Agent

### Purpose

Answers questions about uploaded documents.

### Implementation

RAG pipeline using:

- Spring AI
- PgVectorStore
- EmbeddingModel
- ChatClient

Example:

> Explain chapter 5.

The agent retrieves relevant chunks before calling the LLM.

---

## Question Generator Agent

### Purpose

Creates interview content from documents.

### Output

- Question
- Short answer
- Detailed answer
- Difficulty
- Category
- Tags

### Persistence

After user approval:

- question
- resource
- tags
- project mapping

are stored into Knowledge DB.

---

## Fallback Agent

### Purpose

Answers questions outside TI Knowledge.

Example:

> Explain MCP.

Uses OpenAI Chat Model directly without retrieval.

---

## Agent Routing

| User Request | Agent |
|--------------|-------|
| Find Java questions | SQL |
| Explain uploaded PDF | Document |
| Generate interview questions | Question Generator |
| What is Kubernetes? | Fallback |