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

The frontend is available at `http://localhost:5173`.
