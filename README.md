# MiniShop

MiniShop is a Java Spring Boot microservices e-commerce platform built to practice DevOps, SRE, Platform Engineering, AWS Free Tier, Docker, Terraform, Kubernetes, Helm, CI/CD, monitoring, logging, tracing, security, autoscaling, and autohealing.

This repository currently contains the project foundation and documentation. Application services will be added in later phases.

## Docker

Build and start the complete local platform:

```powershell
cd C:\My-App\docker
Copy-Item .env.example .env
docker compose up -d --build
```

View logs:

```powershell
docker compose logs -f
```

Stop the platform:

```powershell
docker compose down
```

The local platform is available at:

- Frontend: http://localhost:5173
- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Loki readiness: http://localhost:3100/ready
- Grafana: http://localhost:3000

Grafana's Prometheus and Loki datasources are provisioned automatically. See
`monitoring/README.md` for observability instructions and `logging/README.md`
for centralized logging queries and verification.
