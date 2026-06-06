# MiniShop Docker Platform

The Docker Compose stack runs PostgreSQL, the User Service, Product Service,
API Gateway, and React frontend on the shared `minishop-network`.

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

Open the frontend at `http://localhost:5173`.

## View Status and Logs

```powershell
docker compose ps
docker compose logs -f
```

View one service:

```powershell
docker compose logs -f api-gateway
```

## Stop

```powershell
docker compose down
```

To also delete the PostgreSQL volume and all local MiniShop data:

```powershell
docker compose down -v
```
