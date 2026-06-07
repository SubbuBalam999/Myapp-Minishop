# MiniShop Monitoring

MiniShop uses Prometheus to collect Spring Boot Actuator metrics, Loki and
Promtail to centralize application logs, and Grafana to explore both.

## Start the stack

From the Docker directory:

```powershell
cd C:\My-App\docker
Copy-Item .env.example .env
docker compose up -d --build
```

If `.env` already exists, do not overwrite it.

## Access monitoring

- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Loki readiness: http://localhost:3100/ready
- Grafana: http://localhost:3000

Grafana uses the credentials configured by `GRAFANA_ADMIN_USER` and
`GRAFANA_ADMIN_PASSWORD` in `docker/.env`. The example defaults are
`admin` and `admin`.

The Prometheus and Loki datasources are provisioned automatically. Prometheus
is the default datasource. Open **Explore** in Grafana and try:

```promql
up
```

```promql
http_server_requests_seconds_count
```

```promql
jvm_memory_used_bytes
```

Select the Loki datasource and query the centralized application logs:

```logql
{service=~"api-gateway|user-service|product-service|cart-service|order-service"}
```

See `logging/README.md` for service-specific queries and verification steps.

## Check status and logs

```powershell
docker compose ps
docker compose logs -f prometheus loki promtail grafana
```

## Stop the stack

```powershell
docker compose down
```
