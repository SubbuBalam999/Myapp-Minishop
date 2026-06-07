# MiniShop Cart Service

The Cart Service is a Java 21 and Spring Boot 3 microservice that stores cart
items in PostgreSQL. It runs on port `8083`.

## Database

Create the local database and user in PostgreSQL:

```sql
CREATE USER minishop WITH PASSWORD 'minishop';
CREATE DATABASE minishop_cart OWNER minishop;
```

Flyway creates the `cart_items` table automatically.

## Run and test

```powershell
cd C:\My-App\apps\cart-service
mvn test
mvn spring-boot:run
```

```powershell
curl.exe http://localhost:8083/health
curl.exe http://localhost:8083/api/cart/1

curl.exe -X POST http://localhost:8083/api/cart/items `
  -H "Content-Type: application/json" `
  -d '{"userId":1,"productId":1,"quantity":2}'

curl.exe -X DELETE http://localhost:8083/api/cart/items/1
curl.exe -X DELETE http://localhost:8083/api/cart/1
```
