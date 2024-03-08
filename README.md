# K6 Dashboard

## Uploading Reports

Get started by running `docker compose -f dev/docker-compose.yml up -d` from within the root of this repository.

This will start a [TimescaleDB](https://www.timescale.com/), run the [contained k6 test](./src/test/k6/script.js) and
export the resulting data into the database.

Note that all passwords have been hardcoded for development purposes.
Make sure to replace them when running in production.

### Using Podman

The same is possible using `podman` (starting one pod after the other):

```shell
mkdir -p dev/timescaledb/data

podman network create k6_dashboard

podman run -d --name timescaledb \
  -e POSTGRES_PASSWORD=KrPPCHdYSXz6wMct5tUK \
  -e POSTGRES_USER=k6_dashboard \
  -e POSTGRES_DB=k6_dashboard \
  -v "$(pwd)/dev/timescaledb/data:/var/lib/postgresql/data" \
  --network k6_dashboard \
  -p 5432:5432 \
  timescale/timescaledb:latest-pg16

podman run -d --name k6 \
  --add-host=timescaledb:$(hostname -I | awk '{print $1}') \
  -e TIMESCALEDB_JDBC_URL=postgresql://k6_dashboard:KrPPCHdYSXz6wMct5tUK@timescaledb:5432/k6_dashboard \
  -v "$(pwd)/src/test/k6:/scripts" \
  -v "$(pwd)/dev/k6/entrypoint.sh:/usr/local/bin/entrypoint.sh" \
  --entrypoint /usr/local/bin/entrypoint.sh \
  --network k6_dashboard \
  golang:alpine3.19

podman run -d --name grafana \
  -e GF_AUTH_ANONYMOUS_ORG_ROLE=Admin \
  -e GF_AUTH_ANONYMOUS_ENABLED=true \
  -e GF_AUTH_BASIC_ENABLED=false \
  -v "$(pwd)/dev/grafana:/etc/grafana/provisioning/" \
  --network k6_dashboard \
  -p 3000:3000 \
  grafana/grafana:10.3.1
```

### Using the REST API

The above commands will additionally create a JSON report.
You can export it from the `k6` container after running, the upload it to the running `k6-report-ingress` application.
Replace `docker` by `podman` when running on Linux.

```shell
docker cp k6:report.json "$(pwd)/src/test/k6/report.json"
curl -X POST -F "reportFile=@$(pwd)/src/test/k6/report.json" http://localhost:8080/api/rest/v1/k6/reports
```
