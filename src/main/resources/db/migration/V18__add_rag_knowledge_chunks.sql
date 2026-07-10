CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_chunks (
                                  id BIGSERIAL PRIMARY KEY,

                                  source_file VARCHAR(255) NOT NULL,

                                  section_title VARCHAR(255),

                                  content TEXT NOT NULL,

                                  embedding vector(1536),

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX knowledge_chunks_source_file_idx
    ON knowledge_chunks(source_file);