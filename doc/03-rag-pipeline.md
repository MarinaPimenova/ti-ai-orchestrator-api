# RAG Pipeline

## Supported Formats

- PDF
- DOCX
- CSV

## Processing Flow

```text
Upload Document
       │
       ▼
Apache Tika Extraction
       │
       ▼
Text Splitter
(500–800 tokens)
       │
       ▼
Embedding Model
(OpenAI text-embedding-3-small)
       │
       ▼
pgvector
       │
       ▼
Similarity Search
       │
       ▼
LLM + Retrieved Chunks
       │
       ▼
Final Answer
```

## LangChain4j + Spring AI

Recommended implementation:

- Spring AI Document Readers
- Apache Tika parser
- TokenTextSplitter
- PgVectorStore
- OpenAI Embedding Model
- ChatClient

## Metadata

Each chunk stores metadata:

| Field | Description |
|--------|-------------|
| documentId | Uploaded file |
| filename | Original name |
| page | Page number |
| chunk | Chunk index |
| projectId | TI project |
| uploadedBy | User |