# MiniShop API Gateway

The API Gateway is a Java 21, Spring Boot 3, and Spring Cloud Gateway service. It runs on port `8080` and forwards MiniShop API requests to the backend services.

## Routes

| Gateway path | Destination |
| --- | --- |
| `/api/users/**` | `http://localhost:8081` |
| `/api/products/**` | `http://localhost:8082` |

The destinations can be overridden with:

| Variable | Default |
| --- | --- |
| `USER_SERVICE_URL` | `http://localhost:8081` |
| `PRODUCT_SERVICE_URL` | `http://localhost:8082` |
| `FRONTEND_URL` | `http://localhost:5173` |

The gateway allows the configured frontend origin to call `/api/**` using
`GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS` with `Content-Type` and
`Authorization` headers.

## Prerequisites

- Java 21
- Maven 3.6.3 or later
- User Service running on port `8081`
- Product Service running on port `8082`

## Run

Start the User Service in one PowerShell window:

```powershell
cd C:\My-App\apps\user-service
mvn spring-boot:run
```

Start the Product Service in another PowerShell window:

```powershell
cd C:\My-App\apps\product-service
mvn spring-boot:run
```

Start the API Gateway in a third PowerShell window:

```powershell
cd C:\My-App\apps\api-gateway
mvn spring-boot:run
```

## Automated Tests

The automated tests start temporary local downstream servers, so PostgreSQL and the other MiniShop services are not required:

```powershell
cd C:\My-App\apps\api-gateway
mvn test
```

## Manual Tests

With all three services running:

```powershell
Invoke-RestMethod http://localhost:8080/health
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-WebRequest http://localhost:8080/actuator/prometheus

Invoke-RestMethod http://localhost:8080/api/users
Invoke-RestMethod http://localhost:8080/api/products

$userBody = @{
    name = "Ada Lovelace"
    email = "ada@example.com"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8080/api/users `
    -ContentType "application/json" `
    -Body $userBody

$productBody = @{
    name = "Mechanical Keyboard"
    description = "Compact mechanical keyboard"
    price = 89.99
    stockQuantity = 12
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8080/api/products `
    -ContentType "application/json" `
    -Body $productBody

Invoke-RestMethod http://localhost:8080/api/users/1
Invoke-RestMethod http://localhost:8080/api/products/1
```

## Packaged Application

```powershell
mvn clean package
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```
