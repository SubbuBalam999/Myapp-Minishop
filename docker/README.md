# MiniShop Docker Platform

The Docker Compose stack runs PostgreSQL, the User Service, Product Service,
API Gateway, React frontend, Prometheus, Loki, Promtail, and Grafana on the
shared `minishop-network`.

## Configure

From the Docker directory:

```powershell
cd C:\My-App\docker
Copy-Item .env.example .env
```

The example credentials are intended for local development only.

## Build and Start

```powershell
docker compose up -d --build
```

Open the services at:

- Frontend: http://localhost:5173
- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Loki readiness: http://localhost:3100/ready
- Grafana: http://localhost:3000

Grafana uses `GRAFANA_ADMIN_USER` and `GRAFANA_ADMIN_PASSWORD` from `.env`.
The example credentials are `admin` / `admin`. The Prometheus and Loki
datasources are configured automatically.

## View Status and Logs

```powershell
docker compose ps
docker compose logs -f
```

View one service:

```powershell
docker compose logs -f api-gateway
docker compose logs -f prometheus loki promtail grafana
```

In Grafana **Explore**, select Loki and query `{service="api-gateway"}`.
See `../logging/README.md` for all logging queries and verification steps.

## Stop

```powershell
docker compose down
```

To also delete the PostgreSQL volume and all local MiniShop data:

```powershell
docker compose down -v
```
