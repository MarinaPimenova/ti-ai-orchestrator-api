# Task#2 - Java General Task 2 - PoC Concept Definition
Today I'd like to present the AI Chatbot PoC for the TI Knowledge Platform. The goal of this solution is to transform our knowledge platform into an intelligent assistant for interview preparation and technical learning.

Business value

The chatbot provides five key capabilities.

First, semantic search. Instead of searching by keywords, users can ask questions naturally, such as "Explain the difference between Docker and Kubernetes", and receive the most relevant interview answer from our knowledge base.

Second, RAG over documents. Users can upload PDF, DOCX, or CSV files, and the chatbot can answer questions based only on the content of those documents.

Third, automatic interview question generation. From uploaded documentation, the AI generates interview questions together with ready-to-interview answers, difficulty levels, and categories.

Fourth, users can save approved AI-generated content into the Knowledge Database, allowing the platform to continuously grow.

Finally, if a question is outside our internal knowledge, the chatbot uses OpenAI as a fallback to provide a general technical answer.

Architecture

The solution follows a microservice architecture.

The React AI Chat UI communicates with the API Gateway, which handles authentication using OAuth2 and JWT. The request is then forwarded to the AI Orchestrator, which is responsible for selecting the appropriate AI agent.

We have four specialized agents:

SQL Agent answers questions using structured data from the PostgreSQL Knowledge Database.

Document Agent implements the RAG pipeline and answers questions about uploaded documents.

Question Generator Agent creates interview questions and answers, then saves approved content into the database.

Fallback Agent uses OpenAI for general software engineering questions.

An additional Evaluator reviews AI responses and measures their correctness and relevance, allowing us to monitor chatbot quality.

Technology stack

The PoC is built with Java 21 and Spring Boot 4.

We use Spring AI for LLM integration, embeddings, and vector store support, while LangChain4j provides agent orchestration and SQL tool capabilities. Documents are processed with Apache Tika, vectors are stored in PostgreSQL with pgvector, and responses are streamed to the UI using Server-Sent Events.

Expected outcome

This PoC demonstrates how AI can significantly improve knowledge discovery, automate interview content creation, and build a scalable enterprise knowledge assistant while maintaining measurable response quality.

Thank you.


