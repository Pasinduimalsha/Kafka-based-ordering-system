# 🚀 Enterprise Kafka-Based Order Processing System

An enterprise-grade, event-driven microservices architecture built with **Apache Kafka**, **Confluent Schema Registry**, **Apache Avro Serialization**, **Java 17**, **Spring Boot 3.x**, **Spring Cloud Gateway**, and **React (Vite)**.

---

## 🏛️ System Architecture

```mermaid
flowchart TD
    subgraph ClientTier ["Frontend Presentation Layer"]
        UI["React Web Application (:3000)\n- Real-time Running Avg KPI\n- Order Ingestion Form & Fault Injector\n- Live Kafka Message Stream Audit Log"]
    end

    subgraph GatewayTier ["API Gateway Layer"]
        GW["Spring Cloud API Gateway (:8000)\n- Centralized Routing & CORS\n- Health & Metrics Aggregation"]
    end

    subgraph Microservices ["Spring Boot Microservices"]
        OS["order-service (:8081)\n- Avro Order Producer\n- REST Ingestion API\n- Fault Injection Engine"]
        AS["analytics-service (:8082)\n- Avro Kafka Listener\n- Real-Time Running Avg Engine\n- Spring Kafka @RetryableTopic & DLQ\n- Server-Sent Events (SSE) Stream"]
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

| Module / Tier | Port | Tech Stack | Responsibility |
| :--- | :--- | :--- | :--- |
| **`common-avro`** | - | Apache Avro 1.11, Maven Plugin | Generates type-safe `com.ordering.avro.Order` POJOs directly from `order.avsc`. |
| **`order-service`** | `8081` (host `8082`) | Spring Boot 3, Spring Kafka, Avro Serializer | Producer microservice for order creation, batch streaming, and fault simulation. |
| **`analytics-service`** | `8082` (host `8083`) | Spring Boot 3, Spring Kafka, Avro Deserializer | Consumer microservice with non-blocking `@RetryableTopic`, `@DltHandler`, and real-time running average engine. |
| **`api-gateway`** | `8080` (host `8000`) | Spring Cloud Gateway | Central API Gateway routing `/api/orders/**` $\rightarrow$ `order-service` and `/api/analytics/**` $\rightarrow$ `analytics-service` with CORS. |
| **`frontend`** | `3000` | React 18, Vite, Modern SaaS Design | Formal enterprise console for order placement, resilience simulation, and live stream audit. |
| **Infrastructure** | `9092, 8081, 8090` | Docker Compose | Kafka Broker, Zookeeper, Confluent Schema Registry (`:8081`), and Kafka UI (`:8090`). |

---

## 🚀 Deployment & Execution

### Prerequisites
- [Docker & Docker Compose](https://www.docker.com/)
- (Optional for local development): Java 17+, Maven 3.9+, Node.js 18+

### Starting the Full Stack
```bash
docker compose up --build -d
```

### Accessing the Services
- 🌐 **Web Dashboard**: [http://localhost:3000](http://localhost:3000)
- 🚪 **API Gateway**: [http://localhost:8000](http://localhost:8000)
- 📊 **Kafka UI Management Console**: [http://localhost:8090](http://localhost:8090)
- 📜 **Schema Registry**: [http://localhost:8081](http://localhost:8081)

### Stopping the Stack
```bash
docker compose down
```

---

## 🎯 Verification & Testing

### Running Maven Unit Tests
```bash
mvn clean test
```

### Demonstration Scenarios
1. **Real-time Running Average Calculation**:
   - Use the **Order Ingestion** form or click **"Stream Standard Orders"** on the dashboard.
   - Observe the **Running Average Price** card update in real-time as each Kafka message is consumed.
2. **Transient Failure & Exponential Backoff Retry**:
   - Click **"Simulate Transient Error"**.
   - Observe the order retried across retry topics (`orders-retry-0`, `orders-retry-1`) with exponential delays before resolving.
3. **Dead Letter Queue (DLQ)**:
   - Click **"Simulate Fatal Error (DLQ)"**.
   - Notice the event immediately marked with the red **DLQ Routed** badge and inspect the record on Kafka UI ([http://localhost:8090](http://localhost:8090)).
