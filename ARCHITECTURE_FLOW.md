# Wealth API Architecture Flow

## Purpose

This document gives a short interview-ready explanation of how the main business flows move through the application.

## Layer Overview

The application follows a standard layered Spring Boot structure:

- Controller: accepts HTTP requests, validates request shape, maps response status codes.
- Service: applies business rules and coordinates persistence.
- Repository: loads and saves entities through Spring Data JPA.
- Domain: represents core business entities such as customer, account, transaction, and holdings.

Request path in one line:

`HTTP request -> Controller -> Service -> Repository -> Database -> Response`

## Business Flow 1: Transfer Money

### Endpoint

- `POST /api/v1/accounts/transfer`

### Request responsibilities

- Accept source account id, destination account id, amount, and `clientRequestId`.
- Validate required fields before entering service logic.

### Controller responsibilities

- `AccountController.transfer(...)` receives the request DTO.
- Spring validation checks `@NotNull`, `@Positive`, and `@NotBlank` rules on `TransferRequest`.
- Controller delegates to `AccountService.transfer(...)`.

### Service responsibilities

`AccountService.transfer(...)` handles the business logic in this order:

1. Reject if source and destination accounts are the same.
2. Check whether `clientRequestId` already exists.
3. Load source and destination accounts.
4. Reject if currencies do not match.
5. Normalize and validate amount.
6. Reject if source balance is insufficient.
7. Debit source account.
8. Credit destination account.
9. Save both account balances.
10. Save transaction history for auditability.
11. Return success or duplicate-request message.

### Repository interaction

- `AccountRepository` loads and saves both accounts.
- `AccountTransactionRepository` checks idempotency and stores transaction records.

### Error paths

- Unknown account -> `ResourceNotFoundException` -> HTTP `404`
- Invalid payload -> validation error -> HTTP `400`
- Same account, insufficient balance, or currency mismatch -> `BusinessException` -> HTTP `422`

### Why this matters in interviews

- Shows real payment safety logic.
- Demonstrates idempotency.
- Demonstrates audit trail creation.
- Demonstrates separation of HTTP concerns from business rules.

## Business Flow 2: Rebalance Preview

### Endpoint

- `POST /api/v1/portfolios/{customerId}/rebalance-preview`

### Goal

Generate an advisory view of current allocation versus target allocation based on customer risk profile.

### Controller responsibilities

- `PortfolioController.rebalancePreview(...)` receives `customerId`.
- Delegates to `PortfolioService.rebalancePreview(...)`.

### Service responsibilities

`PortfolioService.rebalancePreview(...)` handles the business logic in this order:

1. Load and validate the customer.
2. Load all holdings for the customer.
3. Sum holding values to compute total market value.
4. Calculate current allocation percentages by symbol.
5. Determine target allocation from risk profile.
6. Build a recommendation response with guidance text.

### Repository interaction

- `CustomerRepository` loads the customer and risk profile.
- `PortfolioHoldingRepository` loads holdings by customer id.

### Risk profile mapping

- Conservative -> more bonds, lower equity
- Balanced -> mixed allocation
- Aggressive -> high equity, low cash

### Why this matters in interviews

- Shows domain-driven business logic beyond CRUD.
- Demonstrates computed API responses.
- Demonstrates how business rules can be expressed in service methods.

## Test Strategy Mapping

The project now has three useful test levels:

- MockMvc integration test for existing end-to-end API coverage.
- REST Assured API tests for cleaner HTTP-level business scenario coverage.
- TestNG unit-style service test for focused domain logic.

## Recommended Interview Narrative

Use this order when explaining the system:

1. Describe the API contract.
2. Describe the controller as the HTTP boundary.
3. Describe the service as the business-rule layer.
4. Describe repositories as persistence abstractions.
5. Describe automated tests that cover happy paths and failure paths.