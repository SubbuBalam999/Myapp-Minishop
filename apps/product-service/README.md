# MiniShop Product Service

The Product Service is a Java 21 and Spring Boot 3 microservice that stores and retrieves MiniShop products. It runs on port `8082`.

## Prerequisites

- Java 21
- Maven 3.6.3 or later
- PostgreSQL

## Database

Create the local database and user in PostgreSQL:

```sql
CREATE USER minishop WITH PASSWORD 'minishop';
CREATE DATABASE minishop_products OWNER minishop;
```

If the `minishop` user already exists, only create the database.

The default connection values can be overridden with these environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/minishop_products` |
| `DB_USERNAME` | `minishop` |
| `DB_PASSWORD` | `minishop` |

Flyway creates the `products` table automatically when the service starts.

## Run

From `apps/product-service`:

```powershell
mvn spring-boot:run
```

Or build and run the packaged application:

```powershell
mvn clean package
java -jar target/product-service-0.0.1-SNAPSHOT.jar
```

## Test

```powershell
mvn test
```

With the service running:

```powershell
Invoke-RestMethod http://localhost:8082/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/prometheus
Invoke-RestMethod http://localhost:8082/api/products

$body = @{
    name = "Mechanical Keyboard"
    description = "Compact mechanical keyboard"
    price = 89.99
    stockQuantity = 12
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8082/api/products `
    -ContentType "application/json" `
    -Body $body

Invoke-RestMethod http://localhost:8082/api/products/1
```
