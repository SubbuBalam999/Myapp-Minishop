# MiniShop Frontend

The MiniShop frontend is built with React 19, Vite, TypeScript, React Router, and Axios.

## Prerequisites

- Node.js 20.19 or later
- npm
- MiniShop API Gateway running on `http://localhost:8080`

## Environment

Copy the example environment file:

```powershell
Copy-Item .env.example .env.local
```

The default configuration is:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Install and Run

```powershell
cd C:\My-App\apps\frontend
npm.cmd install
npm.cmd run dev
```

Open `http://localhost:5173`.

The frontend provides Home, Users, Products, Cart, Orders, Inventory, and
Payments pages. Cart items can be converted into an order, the Orders page
shows history, the Inventory page shows stock by product ID, and the Payments
page creates simulated payments for orders and supports refunds. All API
requests use `VITE_API_BASE_URL`, which should point to the API Gateway rather
than an individual backend service.

## Build

```powershell
npm.cmd run build
```

## Preview the Production Build

```powershell
npm.cmd run preview
```
