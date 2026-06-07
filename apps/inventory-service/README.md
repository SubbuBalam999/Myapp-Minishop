# MiniShop Inventory Service

The Inventory Service is a Java 21 and Spring Boot 3.5 service running on port
`8085`. It owns the `minishop_inventory` PostgreSQL database.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/health` | Lightweight service health |
| `GET` | `/api/inventory` | List inventory by product ID |
| `GET` | `/api/inventory/{productId}` | Get inventory for a product |
| `POST` | `/api/inventory` | Create inventory for a product |
| `POST` | `/api/inventory/reserve` | Move available stock to reserved |
| `POST` | `/api/inventory/release` | Move reserved stock back to available |

Each product can have one inventory record. Reserve and release operations lock
the product inventory row while updating it. Insufficient available or reserved
stock returns `409 Conflict`.

## Run

```powershell
cd C:\My-App\apps\inventory-service
mvn spring-boot:run
```

Environment overrides:

```text
DB_URL=jdbc:postgresql://localhost:5432/minishop_inventory
DB_USERNAME=minishop
DB_PASSWORD=minishop
```

## Test

```powershell
mvn test
```

## Example requests

```powershell
$inventory = @{
    productId = 1
    availableQuantity = 10
    reservedQuantity = 0
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8085/api/inventory `
    -ContentType "application/json" `
    -Body $inventory

$adjustment = @{
    productId = 1
    quantity = 2
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8085/api/inventory/reserve `
    -ContentType "application/json" `
    -Body $adjustment

Invoke-RestMethod `
    -Method Post `
    -Uri http://localhost:8085/api/inventory/release `
    -ContentType "application/json" `
    -Body $adjustment
```
