# 🔔 Notification Service — ChaloKharido Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Consumer-231F20.svg?logo=apachekafka)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker)](https://www.docker.com/)

Asynchronous notification consumer service for the **ChaloKharido** e-commerce platform. Built with **Spring Boot 3** and **Apache Kafka** on port `8085`, consuming transactional lifecycle events to trigger email and alert dispatches.

---

## 🌟 Key Features

- **⚡ Asynchronous Event Consumer**: Subscribes to Kafka topics under `notification-group` to process order confirmations and payment alerts without blocking checkout flow.
- **🛡️ Decoupled Reliability**: Ensures customer communications are resilient even during peak traffic spikes.
- **🐳 Containerized**: Dockerized for instant container deployment.

---

## 🏗️ Architecture

```mermaid
flowchart LR
    Kafka[(Apache Kafka :9092)] -->|OrderPlaced / Payment Events| NotifService[Notification Service :8085]
    NotifService -->|Trigger Notifications| Customer([Email / Alerts])
```

---

## ⚙️ Configuration

Configured via `application.properties`:

```properties
spring.application.name=notification-service
server.port=8085

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
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
docker build -t notification-service:latest .

# Run Container
docker run -d -p 8085:8085 --name notification-service notification-service:latest
```

---

## 👤 Author
* **Suzain Naaz**
* GitHub: [@suzain2024](https://github.com/suzain2024)
