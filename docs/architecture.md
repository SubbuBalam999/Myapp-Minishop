# Architecture

MiniShop is planned as a Java Spring Boot microservices e-commerce platform.

## Components

- Frontend
- API Gateway
- User Service
- Product Service
- Cart Service
- Order Service
- Inventory Service
- Payment Service
- Notification Service

The frontend reaches the User, Product, Cart, Order, Inventory, and Payment
services through the API Gateway. Each service owns its PostgreSQL database and
exposes Actuator metrics to Prometheus. Promtail forwards service container
logs to Loki for querying in Grafana.

Notification and asynchronous messaging remain planned for later phases.
