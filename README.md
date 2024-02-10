# K6 Dashboard

## Uploading Reports

Get started by running `docker compose -f dev/docker-compose.yml up -d` from within the root of this repository.

This will start a [TimescaleDB](https://www.timescale.com/), run the [contained k6 test](./src/test/k6/script.js) and
export the resulting data into the database.

### Using Podman

The same is possible using `podman` (starting one pod after the other):

```shell
mkdir -p dev/timescaledb/data

podman run -d --name timescaledb \
  -e POSTGRES_PASSWORD=KrPPCHdYSXz6wMct5tUK \
  -e POSTGRES_USER=k6_dashboard \
  -e POSTGRES_DB=k6_dashboard \
  -v $(pwd)/dev/timescaledb/data:/var/lib/postgresql/data \
  -p 5432:5432 \
  timescale/timescaledb:latest-pg16

podman run -d --name k6 \
  --add-host=timescaledb:$(hostname -I | awk '{print $1}') \
  -e TIMESCALEDB_JDBC_URL=postgresql://k6_dashboard:KrPPCHdYSXz6wMct5tUK@timescaledb:5432/k6_dashboard \
  -v $(pwd)/src/test/k6:/scripts \
  -v $(pwd)/dev/k6/entrypoint.sh:/usr/local/bin/entrypoint.sh \
  --entrypoint /usr/local/bin/entrypoint.sh \
  golang:alpine3.19
```

### Using the REST API

The below command will create a JSON report which you can then upload to the running application using `curl`.
Replace `docker` by `podman` when running on Linux.

```shell
docker run --rm -i -v $PWD/src/test/k6:/app -w /app grafana/k6 run --out json=report.json script.js
curl -X POST -F "reportFile=@$PWD/src/test/k6/report.json" http://localhost:8080/api/rest/v1/k6/reports
```
