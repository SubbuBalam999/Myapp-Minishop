# MiniShop Centralized Logging

MiniShop uses Loki for log storage, Promtail for Docker log collection, and
Grafana for querying logs. Promtail collects only these Compose services:

- `api-gateway`
- `user-service`
- `product-service`
- `cart-service`
- `order-service`

## Start logging

From the Docker directory:

```powershell
cd C:\My-App\docker
docker compose up -d --build
```

Open Grafana at http://localhost:3000 and select **Explore**. Choose the
automatically provisioned **Loki** datasource, then run:

```logql
{service="api-gateway"}
```

Use the same query with `user-service` or `product-service` to inspect those
logs. Use `cart-service` or `order-service` for commerce workflow logs. To view
all collected MiniShop logs:

```logql
{job="minishop"}
```

Generate traffic before querying if a service has not emitted logs yet:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
Invoke-WebRequest http://localhost:8081/actuator/health
Invoke-WebRequest http://localhost:8082/actuator/health
```

## Verify the logging stack

```powershell
docker compose ps -a loki-init loki promtail grafana
curl.exe --fail --retry 12 --retry-delay 5 --retry-all-errors http://localhost:3100/ready
curl.exe --fail http://localhost:9080/ready
Invoke-RestMethod http://localhost:3100/loki/api/v1/labels
docker compose logs --tail=100 loki promtail
```

The `loki-init` service should show `Exited (0)`. It prepares the persistent
volume for Loki's non-root user and does not remain running.

Promtail reads the Docker socket and container log directory as read-only
mounts. The setup assumes Docker uses its default `json-file` logging driver.

Promtail reached end of life on March 2, 2026. It is included because this
project explicitly uses Promtail; plan a migration to Grafana Alloy for future
production use.
