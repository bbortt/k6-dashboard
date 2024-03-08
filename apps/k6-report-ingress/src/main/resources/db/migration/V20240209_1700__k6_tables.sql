CREATE TABLE samples
(
    id     BIGSERIAL,
    ts     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metric TEXT                                               NOT NULL,
    tags   JSONB,
    value  REAL
);

CREATE INDEX samples_ts_idx
    ON samples (ts DESC);

CREATE INDEX idx_samples_ts
    ON samples (ts DESC);

CREATE TABLE thresholds
(
    id               BIGSERIAL,
    ts               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metric           VARCHAR(128)                                       NOT NULL,
    tags             JSONB,
    threshold        VARCHAR(128)                                       NOT NULL,
    abort_on_fail    BOOLEAN                  DEFAULT FALSE,
    delay_abort_eval VARCHAR(128),
    last_failed      BOOLEAN                  DEFAULT FALSE
);

CREATE INDEX idx_thresholds_ts
    ON thresholds (ts DESC);
