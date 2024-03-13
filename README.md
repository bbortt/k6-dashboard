# K6 Dashboard

- [Author's Note](#authors-note)
- [Getting Started](#getting-started)
- [Development Requirements](#development-requirements)

## Author's Note

You might wonder, "Why should I use this?" It's a valid question, and here's my take on it.

This project is designed to help organizations navigate challenges such as network complexities, duty segregation, and
access restrictions - the kind of issues larger companies often face. It's about not having to worry about every
developer having direct access to sensitive metrics or the hesitance of observability teams to share access credentials.

The solution? The `k6-report-ingress` service. It's a straightforward HTTP service that takes a `k6` JSON report and
stores it in a TimeScale database. This setup keeps your database secure and still lets anyone submit their k6 reports
for persistence. Easy and secure.

Then there's the issue of data visualization and comparison, which can be tricky with time-series data and tools that
don't alter the data. And not everyone wants to invest in costly cloud services for this purpose. That's where
the `dash-board` comes in, simplifying the process of visualizing your data.

Enjoy exploring the project!

## Getting Started

To kick things off, run `docker compose -f dev/docker-compose.yaml up -d` from the project's root directory.

This command sets up a [TimescaleDB](https://www.timescale.com/), executes the included `k6` test, and stores the
results in the database.

Note: Passwords in the development setup are hardcoded. Remember to change them for production use.

### Using Podman

Alternatively, you can use `podman` to set up the environment step by step:

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

### REST Endpoint Usage

Following the commands above, a JSON report will be generated. You can extract it from the `k6` container and upload it
to the `k6-report-ingress` service.

Using [the CLI](./cli), this is as simple as executing the command below (use `podman` on Linux).

```shell
docker cp k6:report.json "$(pwd)/src/test/k6/report.json"
./cli upload -u http://localhost:8080 "$(pwd)/src/test/k6/report.json"
```

Or, you can still use curl.

```shell
curl -X POST -F "reportFile=@$(pwd)/src/test/k6/report.json" http://localhost:8080/api/rest/v1/k6/reports
```

## Development Requirements

If you're just looking to see the project in action, head over to [Getting Started](#getting-started). If you're
interested in contributing, you'll need:.

- [OpenJDK 21](https://adoptium.net/temurin/releases/) for the [`apps`](./apps) directory
- [Rust](https://www.rust-lang.org/tools/install) for the [`cli`](./cli) directory

### Database Setup

Start by setting up the PostgreSQL database from [`dev/docker-compose.yml`](./dev/docker-compose.yaml). Once it's
running, apply all database migrations with `./gradlew :apps:k6-report-ingress:flywayMigrate`.
