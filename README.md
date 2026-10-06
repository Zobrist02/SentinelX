# SentinelX — Fraud Detection API

SentinelX is a backend fraud detection API built with **Java and Spring Boot** that evaluates financial transactions using a configurable rule-based scoring engine and publishes completed fraud assessments through **Apache Kafka** for downstream processing.

The project focuses on backend engineering concepts commonly used in fintech systems, including **transaction persistence, validation, rule-based decisioning, historical transaction analysis, event-driven processing, and structured exception handling**.

---

## Architecture

```text
                    ┌─────────────────────┐
                    │      Client         │
                    │  REST API Request   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ TransactionService  │
                    └──────────┬──────────┘
                               │
                  ┌────────────┴────────────┐
                  │                         │
                  ▼                         ▼
          ┌───────────────┐       ┌───────────────────┐
          │  PostgreSQL   │       │ FraudDetection    │
          │  Transaction  │       │     Service       │
          │   Storage     │       └─────────┬─────────┘
          └───────────────┘                 │
                                            ▼
                                  ┌───────────────────┐
                                  │    Fraud Rules    │
                                  │                   │
                                  │ • High Amount     │
                                  │ • Suspicious Loc. │
                                  │ • Velocity        │
                                  │ • Location Change │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ Fraud Assessment  │
                                  │ Persistence       │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ Apache Kafka      │
                                  │ transaction-      │
                                  │ assessed-events   │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ FraudAlertConsumer│
                                  │                   │
                                  │ REVIEW / BLOCK    │
                                  │ → Alert Logging   │
                                  └───────────────────┘
```

---

## Key Features

- REST API for transaction submission
- PostgreSQL persistence using Spring Data JPA
- Bean Validation for incoming transaction requests
- Rule-based fraud detection engine
- Multiple independent fraud detection rules
- Weighted fraud scoring system
- `APPROVE`, `REVIEW`, and `BLOCK` decisions
- Historical transaction analysis
- Fraud assessment persistence
- Apache Kafka event publishing
- Kafka consumer for downstream fraud alerts
- Structured global exception handling
- Environment-based database configuration

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Persistence layer |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Apache Kafka | Event-driven processing |
| Docker | Running Kafka locally |
| Maven | Build and dependency management |
| Jakarta Validation | Request validation |

---

# Fraud Detection Engine

SentinelX uses a **rule-based scoring model**.

Each fraud rule independently evaluates a transaction and contributes a weighted score.

The final score determines the transaction decision.

### Decision thresholds

| Fraud Score | Decision |
|---:|---|
| `< 40` | `APPROVE` |
| `40 – 69` | `REVIEW` |
| `>= 70` | `BLOCK` |

The rules are implemented behind a common `FraudRule` interface, allowing additional rules to be introduced without changing the core fraud detection service.

---

## Implemented Fraud Rules

### 1. High Amount Rule

Detects unusually large transactions.

```text
Amount > ₹100,000
        ↓
+40 fraud points
```

---

### 2. Suspicious Location Rule

Flags transactions originating from configured high-risk locations.

Current suspicious locations include:

```text
UNKNOWN
HIGH_RISK
```

```text
Suspicious location
        ↓
+30 fraud points
```

---

### 3. Velocity Rule

Detects unusually high transaction frequency.

The system examines transactions made by the same user within the previous **5 minutes**.

```text
6+ transactions → +30 points
9+ transactions → +50 points
```

This allows SentinelX to identify rapid transaction bursts that may indicate automated or fraudulent activity.

---

### 4. Location Change Rule

Detects rapid geographic changes in transaction activity.

If a user's latest transaction occurs at a different location from their previous transaction within **10 minutes**, the transaction receives:

```text
+40 fraud points
```

This models a simplified version of a **behavioral anomaly / impossible travel** signal.

---

# API

## Submit Transaction

```http
POST /api/transactions
Content-Type: application/json
```

### Request

```json
{
  "userId": 101,
  "amount": 150000,
  "currency": "INR",
  "merchant": "Amazon",
  "location": "Delhi"
}
```

### Response

```json
{
  "transactionId": 42,
  "userId": 101,
  "amount": 150000,
  "currency": "INR",
  "merchant": "Amazon",
  "location": "Delhi",
  "timestamp": "2026-10-06T10:30:00",
  "fraudScore": 40,
  "decision": "REVIEW"
}
```

The transaction is persisted before the fraud assessment is performed.

The fraud assessment is then persisted and the completed assessment is published to Kafka.

---

## Get Fraud Assessments

### Get all assessments

```http
GET /api/fraud-assessments
```

Returns fraud assessments ordered by assessment time.

### Get assessment for a transaction

```http
GET /api/fraud-assessments/{transactionId}
```

Example:

```http
GET /api/fraud-assessments/42
```

If the assessment does not exist, the API returns:

```http
404 Not Found
```

with a structured error response.

---

# Kafka Architecture

Kafka is used as a **downstream event mechanism**, rather than as part of the synchronous fraud scoring path.

The API must immediately return the fraud result to the client, so fraud detection remains synchronous:

```text
REST Request
     ↓
Save Transaction
     ↓
Run Fraud Rules
     ↓
Persist Fraud Assessment
     ↓
Return API Response
```

After the assessment has been generated, SentinelX publishes a `TransactionAssessedEvent` to Kafka.

```text
TransactionAssessedEvent
├── transactionId
├── userId
├── amount
├── currency
├── merchant
├── location
├── timestamp
├── fraudScore
└── decision
```

The Kafka topic is:

```text
transaction-assessed-events
```

A separate `FraudAlertConsumer` listens to this topic.

For:

```text
APPROVE
```

the consumer takes no action.

For:

```text
REVIEW
BLOCK
```

the consumer generates a fraud alert through application logging.

This demonstrates a basic **event-driven architecture** without duplicating the fraud detection process.

---

# Project Structure

```text
src/main/java/com/app/sentinelx
│
├── controller
│   ├── TransactionController
│   └── FraudAssessmentController
│
├── dto
│   ├── TransactionRequest
│   ├── TransactionAssessmentResponse
│   ├── FraudAssessment
│   └── ApiErrorResponse
│
├── entity
│   ├── Transaction
│   ├── FraudAssessmentEntity
│   └── FraudDecision
│
├── repository
│   ├── TransactionRepository
│   └── FraudAssessmentRepository
│
├── service
│   ├── TransactionService
│   └── FraudDetectionService
│
├── service/fraud
│   └── FraudRule implementations
│
├── kafka
│   ├── TransactionAssessedEvent
│   ├── TransactionEventProducer
│   ├── FraudAlertConsumer
│   └── KafkaTopicConfig
│
└── exception
    ├── GlobalExceptionHandler
    └── FraudAssessmentNotFoundException
```

---

# Error Handling

SentinelX uses centralized exception handling through `@RestControllerAdvice`.

The API provides structured error responses for:

- Invalid request data
- Missing fraud assessments
- Unexpected server-side errors

Example:

```json
{
  "timestamp": "2026-10-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "amount: must be greater than or equal to 0.01",
  "path": "/api/transactions"
}
```

This prevents controllers from having to implement repetitive exception-response logic.

---

# Configuration

Database credentials are provided through environment variables.

Example:

```text
DB_PASSWORD=your_database_password
```

The application reads the password through:

```properties
spring.datasource.password=${DB_PASSWORD}
```

Kafka can run locally using Docker.

The application expects Kafka to be available at:

```text
localhost:9092
```

---

# Running the Project

## Prerequisites

Install:

- Java 21+
- Maven
- PostgreSQL
- Docker Desktop

---

## 1. Create the PostgreSQL Database

Create a database named:

```text
fraud_detection
```

Configure the required database environment variable:

```text
DB_PASSWORD=<your-postgres-password>
```

The database username and connection URL can be configured through the application properties.

---

## 2. Start Kafka

Start Docker Desktop and run the Kafka container using the project's Docker Compose configuration.

Kafka will be available at:

```text
localhost:9092
```

---

## 3. Start SentinelX

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will start on the configured Spring Boot port.

---

# Example Fraud Scenarios

## Normal Transaction

```text
Amount:       ₹500
Location:     Delhi
Velocity:     Normal
Location:     No recent change

Score:        0
Decision:     APPROVE
```

---

## High-Value Transaction

```text
Amount:       ₹150,000

High Amount Rule:
+40

Decision:
REVIEW
```

---

## Multiple Risk Signals

A transaction may trigger multiple rules.

For example:

```text
High Amount       +40
Suspicious Location +30
──────────────────────
Total              70

Decision: BLOCK
```

The rules are additive, allowing multiple independent risk signals to contribute to a single decision.

---

# Design Decisions

### Why a rule-based engine?

The goal of SentinelX is to demonstrate a transparent and explainable fraud detection approach.

Each rule represents an independent fraud signal, making the scoring process easy to understand and extend.

A production system could later replace or complement these rules with statistical or machine-learning models.

### Why Kafka?

Fraud assessment itself needs to be returned immediately through the API, so making the entire detection process asynchronous would complicate the API contract.

Kafka is therefore used for **downstream event processing**.

This allows additional consumers—such as notification, analytics, audit, or reporting services—to react to completed fraud assessments without coupling them directly to the transaction API.

### Why PostgreSQL?

Fraud assessments and transactions have structured relationships and require durable persistence, making a relational database appropriate for this use case.

---

# Future Improvements

Potential extensions include:

- Redis-based distributed rate limiting
- Additional behavioral fraud rules
- Machine-learning-based fraud scoring
- Fraud alert persistence
- Notification service
- Authentication and authorization
- Kafka dead-letter handling
- Transaction event retry mechanisms
- Database migrations using Flyway
- Distributed tracing and metrics

These are intentionally outside the current scope of SentinelX.

---

# Project Goal

SentinelX was built to demonstrate practical backend engineering concepts relevant to **Software Developer / Backend Developer roles**, particularly in fintech-oriented systems.

The project emphasizes:

- REST API development
- Spring Boot architecture
- Database persistence
- Business-rule implementation
- Historical data analysis
- Event-driven architecture
- Kafka producer/consumer workflows
- Input validation
- Exception handling
- Clean separation of responsibilities# SentinelX — Fraud Detection API

SentinelX is a backend fraud detection API built with **Java and Spring Boot** that evaluates financial transactions using a configurable rule-based scoring engine and publishes completed fraud assessments through **Apache Kafka** for downstream processing.

The project focuses on backend engineering concepts commonly used in fintech systems, including **transaction persistence, validation, rule-based decisioning, historical transaction analysis, event-driven processing, and structured exception handling**.

---

## Architecture

```text
                    ┌─────────────────────┐
                    │      Client         │
                    │  REST API Request   │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ TransactionService  │
                    └──────────┬──────────┘
                               │
                  ┌────────────┴────────────┐
                  │                         │
                  ▼                         ▼
          ┌───────────────┐       ┌───────────────────┐
          │  PostgreSQL   │       │ FraudDetection    │
          │  Transaction  │       │     Service       │
          │   Storage     │       └─────────┬─────────┘
          └───────────────┘                 │
                                            ▼
                                  ┌───────────────────┐
                                  │    Fraud Rules    │
                                  │                   │
                                  │ • High Amount     │
                                  │ • Suspicious Loc. │
                                  │ • Velocity        │
                                  │ • Location Change │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ Fraud Assessment  │
                                  │ Persistence       │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ Apache Kafka      │
                                  │ transaction-      │
                                  │ assessed-events   │
                                  └─────────┬─────────┘
                                            │
                                            ▼
                                  ┌───────────────────┐
                                  │ FraudAlertConsumer│
                                  │                   │
                                  │ REVIEW / BLOCK    │
                                  │ → Alert Logging   │
                                  └───────────────────┘
```

---

## Key Features

- REST API for transaction submission
- PostgreSQL persistence using Spring Data JPA
- Bean Validation for incoming transaction requests
- Rule-based fraud detection engine
- Multiple independent fraud detection rules
- Weighted fraud scoring system
- `APPROVE`, `REVIEW`, and `BLOCK` decisions
- Historical transaction analysis
- Fraud assessment persistence
- Apache Kafka event publishing
- Kafka consumer for downstream fraud alerts
- Structured global exception handling
- Environment-based database configuration

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Persistence layer |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Apache Kafka | Event-driven processing |
| Docker | Running Kafka locally |
| Maven | Build and dependency management |
| Jakarta Validation | Request validation |

---

# Fraud Detection Engine

SentinelX uses a **rule-based scoring model**.

Each fraud rule independently evaluates a transaction and contributes a weighted score.

The final score determines the transaction decision.

### Decision thresholds

| Fraud Score | Decision |
|---:|---|
| `< 40` | `APPROVE` |
| `40 – 69` | `REVIEW` |
| `>= 70` | `BLOCK` |

The rules are implemented behind a common `FraudRule` interface, allowing additional rules to be introduced without changing the core fraud detection service.

---

## Implemented Fraud Rules

### 1. High Amount Rule

Detects unusually large transactions.

```text
Amount > ₹100,000
        ↓
+40 fraud points
```

---

### 2. Suspicious Location Rule

Flags transactions originating from configured high-risk locations.

Current suspicious locations include:

```text
UNKNOWN
HIGH_RISK
```

```text
Suspicious location
        ↓
+30 fraud points
```

---

### 3. Velocity Rule

Detects unusually high transaction frequency.

The system examines transactions made by the same user within the previous **5 minutes**.

```text
6+ transactions → +30 points
9+ transactions → +50 points
```

This allows SentinelX to identify rapid transaction bursts that may indicate automated or fraudulent activity.

---

### 4. Location Change Rule

Detects rapid geographic changes in transaction activity.

If a user's latest transaction occurs at a different location from their previous transaction within **10 minutes**, the transaction receives:

```text
+40 fraud points
```

This models a simplified version of a **behavioral anomaly / impossible travel** signal.

---

# API

## Submit Transaction

```http
POST /api/transactions
Content-Type: application/json
```

### Request

```json
{
  "userId": 101,
  "amount": 150000,
  "currency": "INR",
  "merchant": "Amazon",
  "location": "Delhi"
}
```

### Response

```json
{
  "transactionId": 42,
  "userId": 101,
  "amount": 150000,
  "currency": "INR",
  "merchant": "Amazon",
  "location": "Delhi",
  "timestamp": "2026-10-06T10:30:00",
  "fraudScore": 40,
  "decision": "REVIEW"
}
```

The transaction is persisted before the fraud assessment is performed.

The fraud assessment is then persisted and the completed assessment is published to Kafka.

---

## Get Fraud Assessments

### Get all assessments

```http
GET /api/fraud-assessments
```

Returns fraud assessments ordered by assessment time.

### Get assessment for a transaction

```http
GET /api/fraud-assessments/{transactionId}
```

Example:

```http
GET /api/fraud-assessments/42
```

If the assessment does not exist, the API returns:

```http
404 Not Found
```

with a structured error response.

---

# Kafka Architecture

Kafka is used as a **downstream event mechanism**, rather than as part of the synchronous fraud scoring path.

The API must immediately return the fraud result to the client, so fraud detection remains synchronous:

```text
REST Request
     ↓
Save Transaction
     ↓
Run Fraud Rules
     ↓
Persist Fraud Assessment
     ↓
Return API Response
```

After the assessment has been generated, SentinelX publishes a `TransactionAssessedEvent` to Kafka.

```text
TransactionAssessedEvent
├── transactionId
├── userId
├── amount
├── currency
├── merchant
├── location
├── timestamp
├── fraudScore
└── decision
```

The Kafka topic is:

```text
transaction-assessed-events
```

A separate `FraudAlertConsumer` listens to this topic.

For:

```text
APPROVE
```

the consumer takes no action.

For:

```text
REVIEW
BLOCK
```

the consumer generates a fraud alert through application logging.

This demonstrates a basic **event-driven architecture** without duplicating the fraud detection process.

---

# Project Structure

```text
src/main/java/com/app/sentinelx
│
├── controller
│   ├── TransactionController
│   └── FraudAssessmentController
│
├── dto
│   ├── TransactionRequest
│   ├── TransactionAssessmentResponse
│   ├── FraudAssessment
│   └── ApiErrorResponse
│
├── entity
│   ├── Transaction
│   ├── FraudAssessmentEntity
│   └── FraudDecision
│
├── repository
│   ├── TransactionRepository
│   └── FraudAssessmentRepository
│
├── service
│   ├── TransactionService
│   └── FraudDetectionService
│
├── service/fraud
│   └── FraudRule implementations
│
├── kafka
│   ├── TransactionAssessedEvent
│   ├── TransactionEventProducer
│   ├── FraudAlertConsumer
│   └── KafkaTopicConfig
│
└── exception
    ├── GlobalExceptionHandler
    └── FraudAssessmentNotFoundException
```

---

# Error Handling

SentinelX uses centralized exception handling through `@RestControllerAdvice`.

The API provides structured error responses for:

- Invalid request data
- Missing fraud assessments
- Unexpected server-side errors

Example:

```json
{
  "timestamp": "2026-10-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "amount: must be greater than or equal to 0.01",
  "path": "/api/transactions"
}
```

This prevents controllers from having to implement repetitive exception-response logic.

---

# Configuration

Database credentials are provided through environment variables.

Example:

```text
DB_PASSWORD=your_database_password
```

The application reads the password through:

```properties
spring.datasource.password=${DB_PASSWORD}
```

Kafka can run locally using Docker.

The application expects Kafka to be available at:

```text
localhost:9092
```

---

# Running the Project

## Prerequisites

Install:

- Java 21+
- Maven
- PostgreSQL
- Docker Desktop

---

## 1. Create the PostgreSQL Database

Create a database named:

```text
fraud_detection
```

Configure the required database environment variable:

```text
DB_PASSWORD=<your-postgres-password>
```

The database username and connection URL can be configured through the application properties.

---

## 2. Start Kafka

Start Docker Desktop and run the Kafka container using the project's Docker Compose configuration.

Kafka will be available at:

```text
localhost:9092
```

---

## 3. Start SentinelX

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will start on the configured Spring Boot port.

---

# Example Fraud Scenarios

## Normal Transaction

```text
Amount:       ₹500
Location:     Delhi
Velocity:     Normal
Location:     No recent change

Score:        0
Decision:     APPROVE
```

---

## High-Value Transaction

```text
Amount:       ₹150,000

High Amount Rule:
+40

Decision:
REVIEW
```

---

## Multiple Risk Signals

A transaction may trigger multiple rules.

For example:

```text
High Amount       +40
Suspicious Location +30
──────────────────────
Total              70

Decision: BLOCK
```

The rules are additive, allowing multiple independent risk signals to contribute to a single decision.

---

# Design Decisions

### Why a rule-based engine?

The goal of SentinelX is to demonstrate a transparent and explainable fraud detection approach.

Each rule represents an independent fraud signal, making the scoring process easy to understand and extend.

A production system could later replace or complement these rules with statistical or machine-learning models.

### Why Kafka?

Fraud assessment itself needs to be returned immediately through the API, so making the entire detection process asynchronous would complicate the API contract.

Kafka is therefore used for **downstream event processing**.

This allows additional consumers—such as notification, analytics, audit, or reporting services—to react to completed fraud assessments without coupling them directly to the transaction API.

### Why PostgreSQL?

Fraud assessments and transactions have structured relationships and require durable persistence, making a relational database appropriate for this use case.

---

# Future Improvements

Potential extensions include:

- Redis-based distributed rate limiting
- Additional behavioral fraud rules
- Machine-learning-based fraud scoring
- Fraud alert persistence
- Notification service
- Authentication and authorization
- Kafka dead-letter handling
- Transaction event retry mechanisms
- Database migrations using Flyway
- Distributed tracing and metrics

These are intentionally outside the current scope of SentinelX.

---

# Project Goal

SentinelX was built to demonstrate practical backend engineering concepts relevant to **Software Developer / Backend Developer roles**, particularly in fintech-oriented systems.

The project emphasizes:

- REST API development
- Spring Boot architecture
- Database persistence
- Business-rule implementation
- Historical data analysis
- Event-driven architecture
- Kafka producer/consumer workflows
- Input validation
- Exception handling
- Clean separation of responsibilities