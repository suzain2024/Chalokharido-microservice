# 🛒 Order Service — ChaloKharido Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-231F20.svg?logo=apachekafka)](https://kafka.apache.org/)
[![Netflix Eureka](https://img.shields.io/badge/Eureka-Client-red.svg)](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker)](https://www.docker.com/)

Core order processing and transactional lifecycle microservice for the **ChaloKharido** e-commerce platform. Built with **Spring Boot 3**, **PostgreSQL** (`order_db`), and **Apache Kafka** implementing an **Event-Driven Choreography Saga Pattern** on port `8083`.

---

## 🌟 Key Architectural Features

- **📦 Database-per-Service**: Independent `order_db` schema in PostgreSQL for isolated transactional storage.
- **⚡ Event-Driven Choreography Saga**:
  - Emits `OrderPlacedEvent` asynchronously to Apache Kafka upon order checkout.
  - Consumes `PaymentSuccessEvent` or `PaymentFailureEvent` from `payment-events-topic` to update order state (`CONFIRMED` vs `FAILED/CANCELLED`).
- **🛡️ Distributed Fault Tolerance**: Asynchronous event decoupling ensures orders are captured even during downstream payment latency.
- **🔍 Service Discovery**: Registers dynamically with Eureka Server (`8761`).
- **🐳 Containerized**: Dockerized for seamless container deployment.

---

## 🏗️ Saga Workflow Architecture

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant OrderService as Order Service (:8083)
    participant Kafka as Apache Kafka (:9092)
    participant PaymentService as Payment Service (:8084)

    Customer->>OrderService: POST /api/order/place
    OrderService->>OrderService: Save Order (Status: PENDING)
    OrderService->>Kafka: Publish OrderPlacedEvent
    Kafka->>PaymentService: Consume OrderPlacedEvent
    PaymentService->>PaymentService: Process Payment
    PaymentService->>Kafka: Publish PaymentSuccess / PaymentFailure
    Kafka->>OrderService: Consume Payment Event
    OrderService->>OrderService: Update Order (CONFIRMED / CANCELLED)
```

---

## ⚙️ Configuration

Configured via `application.properties`:

```properties
spring.application.name=order-service
server.port=8083

spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Kafka Bootstrap Server
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=order-group

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## 🚀 Running Locally

```bash
# Run with Maven Wrapper
./mvnw clean spring-boot:run
```
*(On Windows: `mvnw.cmd spring-boot:run`)*

---

## 🐳 Docker Deployment

```bash
# Package JAR
./mvnw clean package -DskipTests

# Build Docker Image
docker build -t order-service:latest .

# Run Container
docker run -d -p 8083:8083 --name order-service order-service:latest
```

---

## 👤 Author
* **Suzain Naaz**
* GitHub: [@suzain2024](https://github.com/suzain2024)
