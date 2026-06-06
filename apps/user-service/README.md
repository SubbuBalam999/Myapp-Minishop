# MiniShop User Service

The User Service is a Java 21 and Spring Boot 3 microservice that stores and retrieves MiniShop users. It runs on port `8081`.

## Prerequisites

- Java 21
- Maven 3.6.3 or later
- PostgreSQL

## Database

Create the local database and user in PostgreSQL:

```sql
CREATE USER minishop WITH PASSWORD 'minishop';
CREATE DATABASE minishop_users OWNER minishop;
```

The default connection values can be overridden with these environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/minishop_users` |
| `DB_USERNAME` | `minishop` |
| `DB_PASSWORD` | `minishop` |

Flyway creates the `users` table automatically when the service starts.

## Run

From `apps/user-service`:

```powershell
mvn spring-boot:run
```

Or build and run the packaged application:

```powershell
mvn clean package
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

## Test

```powershell
mvn test
```

With the service running:

```powershell
Invoke-RestMethod http://localhost:8081/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/prometheus
Invoke-RestMethod http://localhost:8081/api/users

$body = @{
    name = "Ada Lovelace"
    email = "ada@example.com"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8081/api/users `
    -ContentType "application/json" `
    -Body $body

Invoke-RestMethod http://localhost:8081/api/users/1
```
