```markdown
# Bank Transaction System

A Java/Spring Boot project that simulates two bank clients (Bank A and Bank B) sending high-volume transaction requests to a central server.

The goal is to:

- Accept JSON transaction requests on each bank client.
- Generate a unique transaction ID per request.
- Convert JSON → XML and forward to a central server **asynchronously**.
- On the server, accept XML, unmarshal to Java, process transactions concurrently, and persist logs to PostgreSQL.
- Reliably handle at least **8000 concurrent requests** from both banks combined.
- Demonstrate thread-safe, race-condition-free processing under load.

---

## 1. Architecture Overview

### Components

- **Server**
  - Spring Boot application
  - Exposes `POST /server/transaction/process` (XML in, JSON out)
  - Uses:
    - Jackson XML for XML ↔ Java conversion
    - `ExecutorService` for multithreaded processing
    - Spring Data JPA + PostgreSQL + HikariCP
    - `ConcurrentHashMap` and DB unique constraint for `trxId` uniqueness

- **Client Bank A**
  - Spring Boot application
  - Exposes `POST /bank/transaction` (JSON)
  - Generates `trxId`, maps to XML model, converts to XML
  - Uses `ExecutorService` for asynchronous forwarding to server
  - Returns JSON acknowledgement (`status: FORWARDED`)

- **Client Bank B**
  - Same behavior as Bank A but with distinct `bankId` and port
  - Exposes `POST /bank/transaction` (JSON)
  - Generates `trxId`, maps to XML model, converts to XML
  - Uses `ExecutorService` for asynchronous forwarding to server
  - Returns JSON acknowledgement (`status: FORWARDED`)

### High-Level Flow

1. JMeter / user sends JSON to:
   - `http://localhost:8081/bank/transaction` (Bank A)
   - `http://localhost:8082/bank/transaction` (Bank B)

2. Each bank client:
   - Generates unique `trxId` (`TRX-YYYYMMDD-000001`).
   - Builds `TransactionRequestXml` object including `BankId`, `CustomerId`, accounts, amount, currency, timestamp.
   - Serializes to XML using Jackson’s `XmlMapper` (isolated in `XmlConverter`).
   - Submits an async task (`ExecutorService`) to forward XML to the central server using `RestTemplate`.
   - Immediately returns:

     ```json
     {
       "trxId": "TRX-YYYYMMDD-000001",
       "status": "FORWARDED",
       "message": "Transaction forwarded to server"
     }
     ```

3. Server:
   - Exposes `POST /server/transaction/process` (consumes `application/xml`, produces `application/json`).
   - Controller delegates to `TransactionOrchestratorService`.
   - `TransactionOrchestratorService` submits processing to a dedicated `ExecutorService` (`transactionExecutor`).
   - Within the pool thread:
     - XML is unmarshalled into `TransactionRequestXml` using `XmlMapper`.
     - `TransactionProcessingService` performs:
       - Basic validation (accounts, amount, currency).
       - In-flight `trxId` guarding via `ConcurrentHashMap`.
       - Business rules (e.g. amount > 100000 → “Insufficient Balance”).
       - Logs the transaction to Postgres via JPA.
       - Handles duplicate `trxId` using DB unique constraint and returns a business failure (“Duplicate Transaction”) instead of a 500 error.
   - Returns JSON:

     ```json
     {
       "trxId": "TRX-YYYYMMDD-000001",
       "status": "SUCCESS" or "FAILED",
       "reason": "Completed / Insufficient Balance / Duplicate Transaction / Validation Error",
       "processingTimeMs": 92
     }
     ```

---

## 2. Project Structure

Repository root:

```text
.
├── README.md
├── server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/org/example/server/
│       │   ├── BankServerApplication.java
│       │   ├── config/ServerConfig.java
│       │   ├── controller/TransactionController.java
│       │   ├── dto/TransactionResponseDto.java
│       │   ├── entity/TransactionLog.java
│       │   ├── model/TransactionRequestXml.java
│       │   ├── repository/TransactionLogRepository.java
│       │   └── service/
│       │       ├── TransactionOrchestratorService.java
│       │       └── TransactionProcessingService.java
│       └── resources/
│           ├── application.yml
│           ├── schema.sql
│           └── seed-data.sql
│
├── client-bank-a/
│   ├── pom.xml
│   └── src/main/
│       ├── java/org/example/server/ClientBankA/
│       │   ├── BankAClientApplication.java
│       │   ├── config/ClientConfig.java
│       │   ├── controller/BankTransactionController.java
│       │   ├── dto/
│       │   │   ├── BankTransactionRequest.java
│       │   │   └── BankTransactionResponse.java
│       │   ├── service/
│       │   │   ├── XmlConverter.java
│       │   │   └── ServerForwarder.java
│       │   ├── util/TransactionIdGenerator.java
│       │   └── xml/TransactionRequestXml.java
│       └── resources/
│           └── application.properties
│
├── client-bank-b/
│   ├── pom.xml
│   └── src/main/
│       ├── java/org/example/server/ClientBankB/
│       │   ├── BankBClientApplication.java
│       │   ├── config/ClientConfig.java
│       │   ├── controller/BankTransactionController.java
│       │   ├── dto/
│       │   │   ├── BankTransactionRequest.java
│       │   │   └── BankTransactionResponse.java
│       │   ├── service/
│       │   │   ├── XmlConverter.java
│       │   │   └── ServerForwarder.java
│       │   ├── util/TransactionIdGenerator.java
│       │   └── xml/TransactionRequestXml.java
│       └── resources/
│           └── application.properties
│
├── database/
│   └── schema.sql
│
└── requests/
    ├── sample-bank-request.json
    └── sample-server-request.xml

bank-load-test.jmx
```

---

## 3. JMeter Load Test Configuration

### Test Plan

- **Test Plan name**: `Bank A & B Load Test`
- **Thread Groups**:
  1. `Bank A Load`
  2. `Bank B Load`

Each thread group has the same configuration, targeting a different client.

---

### Thread Group: Bank A Load

- **Number of Threads (users)**: `8000`
- **Ramp-up Period (seconds)**: `60`
- **Loop Count**: `2`  
  → Total requests from Bank A = 8000 users × 2 loops = **16,000** samples

**HTTP Sampler: `Bank A Transaction Request`**

- Method: `POST`
- Protocol: `http`
- Server Name or IP: `localhost`
- Port Number: `8081`
- Path: `/bank/transaction`
- Use multipart/form-data: unchecked
- Body Data (raw JSON):

  ```json
  {
    "customerId": 892345,
    "fromAccount": "1234567890",
    "toAccount": "9876543210",
    "amount": 1250.75,
    "currency": "INR"
  }
  ```

**HTTP Header Manager: `Bank A Headers`**

- `Content-Type: application/json`

---

### Thread Group: Bank B Load

- **Number of Threads (users)**: `8000`
- **Ramp-up Period (seconds)**: `60`
- **Loop Count**: `2`  
  → Total requests from Bank B = 8000 users × 2 loops = **16,000** samples

**HTTP Sampler: `Bank B Transaction Request`**

- Method: `POST`
- Protocol: `http`
- Server Name or IP: `localhost`
- Port Number: `8082`
- Path: `/bank/transaction`
- Use multipart/form-data: unchecked
- Body Data (same JSON):

  ```json
  {
    "customerId": 892345,
    "fromAccount": "1234567890",
    "toAccount": "9876543210",
    "amount": 1250.75,
    "currency": "INR"
  }
  ```

**HTTP Header Manager: `Bank B Headers`**

- `Content-Type: application/json`

---

### Listener: Summary Report

- **Listener name**: `Summary Report For Bank Application JMeter Report`
- Type: `Summary Report`
- `bank-load-test.jmx` file is added as reference.
- All proof images are added in the repo under `test-results/images/`.

---

## 4. Testing & Verification Summary (per Assessment)

- **8000 Requests at a time / 8000 RPS**
  - JMeter uses 8000 concurrent virtual users per bank, looping to generate a sustained high load.

- **JMeter**
  - Test plan `bank-load-test.jmx` configured with 2 thread groups (Bank A Load, Bank B Load).
  - Summary Report screenshots are included under `test-results/images/`.

- **Concurrency**
  - Clients:
    - Use `ExecutorService` + `CompletableFuture` for asynchronous forwarding of XML to the server.
  - Server:
    - Uses a dedicated `ExecutorService` for processing.
    - Uses Spring `@Transactional`, `ConcurrentHashMap` + DB unique constraint for `trxId` control.
    - Uses HikariCP for DB connection pooling.

- **Load Distribution**
  - Bank A and Bank B each handle 50% of total load (e.g., 4000 users each if a single loop is used for a total of 8000 requests).

- **Success vs Failure**
  - Measured via:
    - Database (`transaction_log` table: `status` + `reason`).
    - JMeter Error% in the Summary Report.
  - Business failures (e.g., `Duplicate Transaction`, `Insufficient Balance`, validation errors) are logged with `status = FAILED`.

- **Average & Peak Times**
  - From JMeter Summary Report:
    - `Average`, `Min`, `Max`, `Std. Dev.` values.
  - From DB:
    - `AVG`, `MIN`, `MAX` over `processing_time_ms`.

- **Observations Under High Load**
  - Server and DB remained stable.
  - No race conditions on `trxId` because `ConcurrentHashMap` and the DB unique constraint protect against duplicates.
  - Thread pools and the HikariCP connection pool prevent resource exhaustion.

- **Ultimate Goal**
  - System handles ≥8000 concurrent requests from both clients, with correct logging, concurrency control, and data integrity.

---

## 5. Evaluation Criteria Mapping

- **Correct multithreaded processing**
  - Explicit use of `ExecutorService` and async patterns on both client and server.
  - Clear separation between HTTP threads and processing threads.

- **Stability under high concurrency**
  - HikariCP limits DB connections and manages pooling.
  - Multi-layered control for `trxId` uniqueness (`ConcurrentHashMap` + `UNIQUE(trx_id)` constraint).
  - Verified through JMeter high-load tests.

- **Clean project structure**
  - Separate modules: `server/`, `client-bank-a/`, `client-bank-b/`, `database/`, `requests/`.
  - Clear layering: controller, service, DTO, entity, repository.

- **Clear explanation of testing approach**
  - JMeter configuration, parameters, and results are documented in this README.
  - DB queries and screenshots (in `test-results/images/`) demonstrate success vs failure and timings.
  - Observations under load are summarized.

---

## 6. Note on Documentation & AI Assistance

All source code, configuration, and tests in this repository were implemented manually.

This README and some of the documentation text were drafted with the help of an AI assistant **only for documentation purposes**, in order to format and summarize the design, testing approach, and results more clearly.
```
