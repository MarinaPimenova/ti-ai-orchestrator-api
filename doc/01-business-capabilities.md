# Business Capabilities

## Vision

The AI Chatbot transforms the TI Knowledge Platform from a static knowledge repository into an intelligent assistant for interview preparation and technical learning.

## Capabilities

### 1. Semantic Knowledge Search

Users ask questions in natural language instead of searching by keywords.

Example:

> Explain the difference between Docker and Kubernetes.

The chatbot searches the Knowledge DB semantically and returns the best matching interview answer.

**Business value**

- Faster knowledge discovery
- Better search accuracy
- No need to know exact wording

---

### 2. RAG over Uploaded Documents

Users upload:

- PDF
- DOCX
- CSV

The chatbot indexes document content into a vector database and answers questions using only that document.

Example:

> Summarize chapter 3 of this Spring Boot guide.

---

### 3. Interview Question Generator

From uploaded documentation the chatbot automatically generates:

- Interview questions
- Difficulty level
- Category
- Ready-to-interview answer

Example output:

| Question | Level |
|----------|------|
| What is Dependency Injection? | Junior |
| Explain Spring Bean lifecycle | Middle |
| How does Spring AOP work? | Senior |

Generated questions can be reviewed and saved into TI Knowledge DB.

---

### 4. Knowledge Base Enrichment

Accepted AI-generated questions become permanent knowledge articles stored in PostgreSQL.

This continuously expands the organization's interview knowledge.

---

### 5. General Technical Assistant

If no internal knowledge exists, the chatbot uses OpenAI to answer general software engineering questions.

Example:

> What is Model Context Protocol?