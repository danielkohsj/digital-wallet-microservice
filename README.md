# Digital Wallet Microservice

A Spring Boot microservice that simulates a **digital wallet system**, supporting user creation, wallet balance management, fund transfers, and transaction history.

The system ensures **transactional integrity, concurrency safety, and idempotent operations** for financial consistency.

---

# Architecture Overview

The service follows a **layered microservice architecture**:

```
Controller Layer
     ↓
Service Layer
     ↓
Repository Layer
     ↓
Database (PostgreSQL)
```

Key design principles:

* Separation of concerns
* Transactional integrity
* Concurrency protection
* Idempotent financial operations

---

# Tech Stack

| Technology        | Purpose               |
| ----------------- | --------------------- |
| Spring Boot       | Application framework |
| Spring Data JPA   | ORM & database access |
| PostgreSQL        | Persistent storage    |
| Docker            | Database container    |
| SpringDoc OpenAPI | API documentation     |
| Lombok            | Boilerplate reduction |
| Maven             | Build tool            |

---

# Key Features

## User Management

* Create wallet users
* Unique email enforcement
* Automatic wallet initialization

---

## Wallet Operations

* Credit funds
* Debit funds
* Transfer funds between users

---

## Transaction History

Track all wallet operations including:

* Credits
* Debits
* Transfers

---

## Idempotent APIs

Prevents duplicate financial operations using:

```
Idempotency-Key header
```

Ensures safe retries in case of network failures.

---

## Concurrency Protection

Balance updates are protected using pessimistic locking:

```
SELECT ... FOR UPDATE
```

This prevents:

* double spending
* race conditions
* inconsistent balances

---

## Deadlock Prevention

Transfers lock user records in deterministic order to prevent database deadlocks.

---

# Project Structure

```
src/main/java/com/boostbank/wallet

controller
    WalletController

service
    WalletService

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


# Running the Project

## 1. Start PostgreSQL using Docker Compose

Run the following command from the project root:

```bash
docker-compose up -d
```

This will start a PostgreSQL database container configured for the wallet service.

---

## 2. Run the Spring Boot Application

Start the application using Maven:

```bash
mvn spring-boot:run
```

Alternatively, run the `WalletServiceApplication` directly from your IDE.

---

## 3. Access API Documentation (Swagger)

Once the application is running, open:

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

Swagger provides an interactive interface to test the wallet APIs.

---

# Docker Configuration

The project includes a `docker-compose.yml` file that provisions a PostgreSQL database for local development.

Example configuration:

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16
    container_name: wallet-postgres
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: walletdb
    ports:
      - "5432:5432"
```


# Example API Requests

## Create User

```
POST /api/wallet/users
```

Request:

```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

---

## Credit Wallet

```
POST /api/wallet/credit
```

Header:

```
Idempotency-Key: credit-001
```

Request:

```json
{
  "userId": "UUID",
  "amount": 100
}
```

---

## Transfer Funds

```
POST /api/wallet/transfer
```

Header:

```
Idempotency-Key: transfer-001
```

Request:

```json
{
  "sourceUserId": "UUID",
  "destinationUserId": "UUID",
  "amount": 50
}
```

---

## Get Wallet Balance

```
GET /api/wallet/balance/{userId}
```

---

## Get Transaction History

```
GET /api/wallet/transactions/{userId}
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

Koh Sheng Jie - Backend Engineer Technical Assessment for Boost Bank
