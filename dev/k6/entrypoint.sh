#!/bin/sh
# Install k6 with the timescaledb extension. Run tests afterwards.
# See: https://k6.io/docs/results-output/real-time/timescaledb.

# Install xk6
go install go.k6.io/xk6/cmd/xk6@latest

# Build the k6 binary
xk6 build --with github.com/grafana/xk6-output-timescaledb

test_id=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 7)
/go/k6 run \
  -o "timescaledb=$TIMESCALEDB_JDBC_URL" \
  --tag "testid=$test_id" \
  /scripts/script.js
