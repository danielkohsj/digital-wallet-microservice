# Digital Wallet Microservice

A **Spring Boot microservice** that simulates a digital wallet system supporting:

* User creation
* Wallet balance management
* Fund transfers between users
* Transaction history tracking

The service is designed with **financial consistency and concurrency safety** in mind, ensuring that wallet operations remain correct even under concurrent requests.

---

# Quick Start

### 1. Start the database

```bash
docker-compose up -d
```

### 2. Run the application

```bash
mvn spring-boot:run
```

### 3. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

# Architecture Overview

The service follows a **layered architecture**:

```
Controller Layer
     ↓
Service Layer
     ↓
Repository Layer
     ↓
PostgreSQL Database
```

### Responsibilities

**Controller Layer**

* Exposes REST APIs
* Handles request validation
* Maps DTOs

**Service Layer**

* Contains business logic
* Handles transactions
* Enforces financial rules

**Repository Layer**

* Manages database persistence
* Uses Spring Data JPA

---

# Tech Stack

| Technology        | Purpose                              |
| ----------------- | ------------------------------------ |
| Spring Boot       | Backend application framework        |
| Spring Data JPA   | ORM and persistence layer            |
| PostgreSQL        | Relational database                  |
| Docker            | Local database container             |
| SpringDoc OpenAPI | Swagger API documentation            |
| Lombok            | Boilerplate code reduction           |
| Maven             | Dependency management and build tool |

---

# Key Features

## User Management

* Create wallet users
* Unique email constraint
* Automatic wallet balance initialization

---

## Wallet Operations

The system supports three core financial operations:

* **Credit** – Add funds to a wallet
* **Debit** – Deduct funds from a wallet
* **Transfer** – Move funds between users

All operations are executed **atomically within a transaction**.

---

## Transaction History

Users can retrieve a complete history of their wallet activity including:

* Incoming transactions
* Outgoing transactions
* Transfers

---

# Concurrency Safety

Wallet systems must prevent **race conditions and double spending**.

This implementation ensures safety using **pessimistic locking**:

```
SELECT ... FOR UPDATE
```

When a wallet balance is updated, the corresponding user record is locked to prevent concurrent modifications.

---

# Deadlock Prevention

During transfers, two user rows must be locked.

To prevent deadlocks, users are always locked in a **deterministic order** based on their UUID values.

Example scenario prevented:

```
Transaction A locks User A then waits for User B
Transaction B locks User B then waits for User A
```

By enforcing consistent locking order, deadlocks are avoided.

---

# Idempotency

Wallet operations support **idempotent requests** using the request header:

```
Idempotency-Key
```

This ensures that duplicate client requests (for example due to network retries) do not cause duplicate financial transactions.

Example:

```
Idempotency-Key: transfer-123
```

The system stores processed keys in the database to detect duplicates.

---

# Project Structure

```
src/main/java/com/boostbank/wallet

controller
    WalletController

service
    WalletService
    UserService

repository
    UserRepository
    TransactionRepository
    IdempotencyKeyRepository

entity
    User
    Transaction
    IdempotencyKey

dto
    request
    response

enums
    TransactionType
    ResultInfo

exception
    GlobalExceptionHandler
```

---

# Running the Project

## Start PostgreSQL

```
docker-compose up -d
```

This launches a PostgreSQL container for local development.

---

## Run the application

```
mvn spring-boot:run
```

Alternatively, run `WalletServiceApplication` from your IDE.

---

# Database Schema

The service uses PostgreSQL with three primary tables.

### users
Stores wallet user information.

| Column  | Type             | Description            |
| ------- | ---------------- | ---------------------- |
| id      | UUID (PK)        | Unique user identifier |
| name    | VARCHAR          | User's name            |
| email   | VARCHAR (UNIQUE) | User email             |
| balance | DECIMAL          | Current wallet balance |


### transactions
Stores all wallet operations

| Column              | Type      | Description                   |
| ------------------- | --------- | ----------------------------- |
| transaction_id      | UUID (PK) | Unique transaction identifier |
| type                | VARCHAR   | CREDIT / DEBIT / TRANSFER     |
| amount              | DECIMAL   | Transaction amount            |
| source_user_id      | UUID      | Sender user ID                |
| destination_user_id | UUID      | Receiver user ID              |
| timestamp           | TIMESTAMP | Transaction timestamp         |


### idempotency_keys
Prevents duplicate financial operations.

| Column          | Type         | Description             |
| --------------- | ------------ | ----------------------- |
| idempotency_key | VARCHAR (PK) | Unique idempotency key  |
| created_at      | TIMESTAMP    | Request processing time |


## Schema Relationships
```
users
^
│
transactions
│
├── source_user_id → users.id
└── destination_user_id → users.id

idempotency_keys
```

---

# Example API Requests

Note: The full API documentation can be found on Swagger, accessible via http://localhost:8080/swagger-ui.html

## Create User

Create a new wallet user.
```
POST /api/wallet/users
```

Request body:

```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "User created successfully",
  "data": {
    "id": "8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1",
    "name": "Alice",
    "email": "alice@example.com",
    "balance": 0
  }
}
```

---

## Credit Wallet

Add funds to a user's wallet.
```
POST /api/wallet/credit
```

Header:

```
Idempotency-Key: credit-001
```

Request body:

```json
{
  "userId": "UUID",
  "amount": 100
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Wallet credited successfully"
}
```

---

## Debit Wallet

Deducts funds from a user's wallet.
```
POST /api/wallet/debit
```

Header:

```
Idempotency-Key: debit-001
```

Request body:

```json
{
  "userId": "UUID",
  "amount": 100
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Wallet debited successfully"
}
```

---

## Transfer Funds

Transfers funds between two users.
```
POST /api/wallet/transfer
```

Header:

```
Idempotency-Key: transfer-001
```

Request body:

```json
{
  "sourceUserId": "UUID",
  "destinationUserId": "UUID2",
  "amount": 50
}
```

Response:
```json
{
  "result": "SUCCESS",
  "message": "Transfer completed successfully"
}
```

---

## Get Wallet Balance

Retrieve the current wallet balance from a user.
```
GET /api/wallet/balance/{userId}
```

Example request:
```
GET /api/wallet/balance/8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1
```

Response:
```json
{
  "result": "SUCCESS",
  "data": {
    "userId": "8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1",
    "balance": 70
  }
}
```

---

## Get Transaction History

Retrieve all transactions associated with a user.
```
GET /api/wallet/transactions/{userId}
```

Example request:
```
GET /api/wallet/transactions/8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1
```

Response:
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "transactionId": "c2a5b1b0-8e91-4c92-a7b0-9d1c8e01e3c7",
      "type": "CREDIT",
      "amount": 100,
      "destinationUserId": "8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1",
      "timestamp": "2026-03-11T10:15:30Z"
    },
    {
      "transactionId": "7eaf3f40-72b7-45f5-8a10-10f55b8e1b22",
      "type": "DEBIT",
      "amount": 30,
      "sourceUserId": "8a0cbb3a-5a60-4a4f-92b0-3fa5c5c3d0e1",
      "timestamp": "2026-03-11T11:20:10Z"
    }
  ]
}
```

---

# Transaction Safety Guarantees

## Atomic Transactions

All wallet operations execute within a database transaction.
If any step fails, the entire operation is rolled back.

---

## Pessimistic Locking

User rows are locked during balance updates:

```
SELECT ... FOR UPDATE
```

This prevents concurrent balance modification.

---

## Idempotency

Duplicate requests are prevented using stored idempotency keys.

---

# Possible Future Improvements

* Event-driven transaction processing
* Ledger-based accounting model
* Distributed idempotency store
* Rate limiting
* Audit logging
* Integration tests

---

# Author

Koh Sheng Jie
