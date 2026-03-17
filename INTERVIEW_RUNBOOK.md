# Wealth API Interview Demo Runbook

## Goal

Use this runbook to demonstrate all 7 API use cases in one clean flow.

## Start

1. Run the app:

```bash
mvn spring-boot:run
```

2. Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

3. Keep the app terminal open for the full demo.

## Demo Data

Use these values unless you need different ones:

```json
{
  "fullName": "Asha Verma",
  "email": "asha.verma.demo1@example.com",
  "riskProfile": "BALANCED",
  "currency": "USD",
  "openingBalanceSource": 1000.00,
  "openingBalanceDestination": 500.00,
  "depositAmount": 100.00,
  "transferAmount": 250.00,
  "clientRequestId": "trf-2026-0001"
}
```

## Demo Steps

### 1. Customer onboarding

- Endpoint: `POST /api/v1/customers`
- Body:

```json
{
  "fullName": "Asha Verma",
  "email": "asha.verma.demo1@example.com",
  "riskProfile": "BALANCED"
}
```

- Save returned `id` as `CUSTOMER_ID`.

### 2. Open source account

- Endpoint: `POST /api/v1/accounts`
- Body:

```json
{
  "customerId": "CUSTOMER_ID",
  "currency": "USD",
  "openingBalance": 1000.00
}
```

- Save returned `id` as `SOURCE_ACCOUNT_ID`.

### 3. Open destination account

- Endpoint: `POST /api/v1/accounts`
- Body:

```json
{
  "customerId": "CUSTOMER_ID",
  "currency": "USD",
  "openingBalance": 500.00
}
```

- Save returned `id` as `DESTINATION_ACCOUNT_ID`.

### 4. Cash deposit

- Endpoint: `POST /api/v1/accounts/{accountId}/deposit`
- Use `SOURCE_ACCOUNT_ID` as the path value.
- Body:

```json
{
  "amount": 100.00,
  "reason": "Monthly savings"
}
```

### 5. Transfer with idempotency

- Endpoint: `POST /api/v1/accounts/transfer`
- Body:

```json
{
  "sourceAccountId": "SOURCE_ACCOUNT_ID",
  "destinationAccountId": "DESTINATION_ACCOUNT_ID",
  "amount": 250.00,
  "clientRequestId": "trf-2026-0001"
}
```

- Expected result: transfer succeeds.
- Optional idempotency proof: send the exact same request again and confirm the second call is ignored.

### 6. Account statement by time window

- Source statement:

```text
GET /api/v1/accounts/SOURCE_ACCOUNT_ID/statement?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z
```

- Destination statement:

```text
GET /api/v1/accounts/DESTINATION_ACCOUNT_ID/statement?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z
```

- Confirm deposit and transfer entries are visible.

### 7. Portfolio holdings update

- Endpoint: `POST /api/v1/portfolios/{customerId}/holdings`
- Use `CUSTOMER_ID` in the path.

First call:

```json
{
  "symbol": "EQUITY",
  "marketValue": 12000.50
}
```

Second call:

```json
{
  "symbol": "DEBT",
  "marketValue": 8000.00
}
```

### 8. Rebalance preview

- Endpoint: `POST /api/v1/portfolios/{customerId}/rebalance-preview`
- Use `CUSTOMER_ID` in the path.
- Body: empty

## Quick Validation

After the main flow, verify these endpoints:

- `GET /api/v1/accounts/SOURCE_ACCOUNT_ID`
- `GET /api/v1/accounts/DESTINATION_ACCOUNT_ID`

Expected balances after the default flow:

- Source: `850.00`
- Destination: `750.00`

## Common Gotchas

- H2 is in-memory, so restarting the app clears all data.
- Reuse ids only within the same app session.
- Source and destination accounts must be different.
- Source and destination currencies must match for transfer.
- `clientRequestId` must be unique for a new transfer.

## Interview Talking Points

- Controller receives HTTP requests and returns responses.
- Service enforces business rules like balance checks and idempotency.
- Repository handles persistence.
- The transfer API uses idempotency to prevent duplicate payment effects.
- Statement endpoints provide auditability.

## Reset

If the demo data gets inconsistent, stop the app, restart it, and repeat the runbook from step 1.