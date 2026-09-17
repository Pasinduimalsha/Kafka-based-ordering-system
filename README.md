# 🚀 Enterprise Kafka-Based Order Processing System

An enterprise-grade, event-driven microservices architecture built with **Apache Kafka**, **Confluent Schema Registry**, **Apache Avro Serialization**, **Java 17**, **Spring Boot 3.x**, **Spring Cloud Gateway**, and **React (Vite)**.

---

## 🏛️ System Architecture

```mermaid
flowchart TD
    subgraph ClientTier ["Frontend Presentation Layer"]
        UI["React Web Application (:3000)\n- Real-time Running Avg KPI\n- Order Generator & Fault Injector\n- Live Kafka Stream & DLQ Inspector"]
    end

    subgraph GatewayTier ["API Gateway Layer"]
        GW["Spring Cloud API Gateway (:8080)\n- Centralized Routing & CORS\n- Health & Metrics Aggregation"]
    end

    subgraph Microservices ["Spring Boot Microservices"]
        OS["order-service (:8081)\n- Avro Order Producer\n- REST Ingestion API\n- Fault Injection Engine"]
        AS["analytics-service (:8082)\n- Avro Kafka Listener\n- Real-Time Running Avg Calculator\n- Spring Kafka @RetryableTopic & DLQ\n- Server-Sent Events (SSE) Stream"]
    end

    subgraph KafkaPlatform ["Event Streaming & Schema Infrastructure (Docker)"]
        KAFKA["Apache Kafka Broker (:9092)"]
        SR["Confluent Schema Registry (:8081)"]
        KUI["Kafka UI Management Console (:8090)"]
    end

    subgraph Topics ["Kafka Topics"]
        T_MAIN["orders (Main Topic)"]
        T_RETRY["orders-retry (Non-blocking Retry Topic)"]
        T_DLQ["orders-dlq (Dead Letter Queue)"]
    end

    UI -->|HTTP / SSE| GW
    GW -->|/api/orders/**| OS
    GW -->|/api/analytics/**| AS

    OS -->|Register / Validate Schema| SR
    OS -->|KafkaAvroSerializer| T_MAIN

    T_MAIN --> AS
    T_RETRY --> AS

    AS -.->|Transient Failure & Retries < 3 (Backoff)| T_RETRY
    AS -.->|Fatal Error or Exhausted Retries| T_DLQ
```

---

## 📜 Avro Schema (`common-avro/src/main/resources/avro/order.avsc`)

The data contract is strictly governed by Schema Registry and compiled into immutable Java POJOs via `avro-maven-plugin`:

```json
{
  "type": "record",
  "name": "Order",
  "namespace": "com.ordering.avro",
  "doc": "Avro schema definition for Order transactions in Kafka",
  "fields": [
    { "name": "orderId", "type": "string", "doc": "Unique identifier for the order" },
    { "name": "product", "type": "string", "doc": "Name of the purchased item" },
    { "name": "price", "type": "float", "doc": "Price of the product" }
  ]
}
```

---

## ⚙️ Microservices & Modules

| Module | Port | Technology | Purpose |
| :--- | :--- | :--- | :--- |
| `common-avro` | - | Apache Avro 1.11 | Generates type-safe `Order` Java classes from `order.avsc`. |
| `api-gateway` | `8080` | Spring Cloud Gateway | Centralized reverse proxy, routing `/api/orders/**` and `/api/analytics/**`. |
| `order-service` | `8081` | Spring Boot 3, Spring Kafka | Producer API with `KafkaAvroSerializer`, Schema Registry, and fault simulation. |
| `analytics-service` | `8082` | Spring Boot 3, Spring Kafka | Consumer with `KafkaAvroDeserializer`, real-time running average engine, `@RetryableTopic`, and DLQ. |
| `frontend` | `3000` | React 18, Vite, Modern CSS | Live visual dashboard, dynamic order creator, fault test panel, and live event feed. |

---

## 🚀 Quickstart & One-Click Deployment

### Prerequisites
- [Docker & Docker Compose](https://www.docker.com/)
- (Optional for local development): Java 17+, Maven 3.9+, Node.js 18+

### Launching the Full Stack (Docker Compose)
```bash
docker compose up --build -d
```

### Accessing the Applications
- 🌐 **React Frontend**: [http://localhost:3000](http://localhost:3000)
- 🚪 **Spring Cloud API Gateway**: [http://localhost:8080](http://localhost:8080)
- 📊 **Kafka UI Web Console**: [http://localhost:8090](http://localhost:8090)
- 📜 **Schema Registry**: [http://localhost:8081](http://localhost:8081)

---

## 🎯 How to Demonstrate the System Live

1. Open **[http://localhost:3000](http://localhost:3000)** in your browser.
2. **Real-Time Running Average**:
   - Click **"Stream 10 Clean Orders"** or use the order form to submit custom orders.
   - Watch the **Running Average Price** card and per-product table update dynamically on each incoming message.
3. **Transient Failure & Exponential Backoff Retry**:
   - Click **"Simulate Transient Failure"**.
   - Observe the order enter the retry topics with exponential backoff (1s, 2s, 4s) before successfully resolving.
4. **Dead Letter Queue (DLQ)**:
   - Click **"Simulate Fatal DLQ Error"**.
   - Notice the order immediately routed to `orders-dlq` with red badge indicators and inspect the record on Kafka UI ([http://localhost:8090](http://localhost:8090)).

---

## 🧪 Running Automated Tests

```bash
mvn clean test
```
