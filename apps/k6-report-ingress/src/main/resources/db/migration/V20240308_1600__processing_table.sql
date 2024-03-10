CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE report_processing
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    start_time        TIMESTAMP WITH TIME ZONE                    NOT NULL,
    end_time          TIMESTAMP WITH TIME ZONE,
    processing_status SMALLINT                                    NOT NULL,
    error_message     TEXT,
    CONSTRAINT chk_processing_status CHECK (processing_status IN (0, 1, 2))
);

ALTER TABLE samples
    ADD COLUMN processing_id UUID,
    ADD FOREIGN KEY (processing_id) REFERENCES report_processing (id);
