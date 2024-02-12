create table samples
(
    id     bigserial,
    ts     timestamp with time zone default CURRENT_TIMESTAMP not null,
    metric text                                               not null,
    tags   jsonb,
    value  real
);

create index samples_ts_idx
    on samples (ts desc);

create index idx_samples_ts
    on samples (ts desc);

create table thresholds
(
    id               bigserial,
    ts               timestamp with time zone default CURRENT_TIMESTAMP not null,
    metric           varchar(128)                                       not null,
    tags             jsonb,
    threshold        varchar(128)                                       not null,
    abort_on_fail    boolean                  default false,
    delay_abort_eval varchar(128),
    last_failed      boolean                  default false
);

create index idx_thresholds_ts
    on thresholds (ts desc);

