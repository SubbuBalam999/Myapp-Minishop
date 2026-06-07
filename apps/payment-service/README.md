# MiniShop Payment Service

The Payment Service is a simulated Java 21 and Spring Boot 3.5 service running
on port `8086`. It owns the `minishop_payments` PostgreSQL database and does not
contact a real payment processor.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/health` | Lightweight service health |
| `GET` | `/api/payments` | List payments newest first |
| `GET` | `/api/payments/{id}` | Get a payment |
| `GET` | `/api/payments/order/{orderId}` | List payments for an order |
| `POST` | `/api/payments` | Create a simulated successful payment |
| `POST` | `/api/payments/{id}/refund` | Refund a successful payment |

New payments use the `SUCCESS` status. Refund requests are idempotent after a
payment reaches `REFUNDED`.

## Run

```powershell
cd C:\My-App\apps\payment-service
mvn spring-boot:run
```

Environment overrides:

```text
DB_URL=jdbc:postgresql://localhost:5432/minishop_payments
DB_USERNAME=minishop
DB_PASSWORD=minishop
```

## Test

```powershell
mvn test
```

## Example requests

```powershell
$payment = @{
    orderId = 1
    userId = 1
    amount = 299.99
    paymentMethod = "CARD"
} | ConvertTo-Json

$created = Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8086/api/payments `
    -ContentType "application/json" `
    -Body $payment

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8086/api/payments/$($created.id)/refund"
```
