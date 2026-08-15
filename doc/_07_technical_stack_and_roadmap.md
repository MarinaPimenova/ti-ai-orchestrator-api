# Recommended Java architecture

For your stack (Java 21 + Spring Boot 4), I'd use both Spring AI and LangChain4j, because they complement each other rather than compete.

## Recommended Java Architecture

For your stack (**Java 21 + Spring Boot 4**), using both **Spring AI** and **LangChain4j** is ideal because they complement each other rather than compete.

| Layer | Library / Technology |
| :--- | :--- |
| **LLM Communication** | Spring AI |
| **Embeddings** | Spring AI |
| **Vector Store** | Spring AI `PgVectorStore` |
| **Agent Orchestration** | LangChain4j |
| **SQL Tool** | LangChain4j Tools |
| **Document Parsing** | Apache Tika |
| **Streaming** | Spring Web MVC + SSE |
| **Observability** | Micrometer + OpenTelemetry |

> **Note:** This architecture is modern, production-oriented, and aligns well with enterprise Java development while keeping the AI logic modular and extensible.

---

## PoC Implementation Roadmap

| Phase | Deliverable |
| :---: | :--- |
| **1** | AI Chat UI with SSE streaming |
| **2** | AI Orchestrator microservice |
| **3** | SQL Agent over Knowledge DB |
| **4** | PDF / DOCX / CSV RAG ingestion |
| **5** | Question Generator Agent + DB persistence |
| **6** | Fallback OpenAI Agent |
| **7** | LLM Evaluator + Prometheus metrics |