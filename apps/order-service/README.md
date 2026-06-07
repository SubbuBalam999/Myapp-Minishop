# MiniShop Order Service

The Order Service is a Java 21 and Spring Boot 3 microservice that stores order
history in PostgreSQL. It runs on port `8084`.

## Database

```sql
CREATE USER minishop WITH PASSWORD 'minishop';
CREATE DATABASE minishop_orders OWNER minishop;
```

Flyway creates the `orders` and `order_items` tables automatically.

## Run and test

```powershell
cd C:\My-App\apps\order-service
mvn test
mvn spring-boot:run
```

```powershell
curl.exe http://localhost:8084/health
curl.exe http://localhost:8084/api/orders
curl.exe http://localhost:8084/api/orders/user/1

curl.exe -X POST http://localhost:8084/api/orders `
  -H "Content-Type: application/json" `
  -d '{"userId":1,"items":[{"productId":1,"productName":"Laptop","quantity":2,"price":999.99}]}'
```
