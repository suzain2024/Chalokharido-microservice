# 🧭 Discovery Service (Netflix Eureka Server) — ChaloKharido

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-blue.svg)](https://spring.io/projects/spring-cloud)
[![Netflix Eureka](https://img.shields.io/badge/Eureka-Server-red.svg)](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker)](https://www.docker.com/)

Centralized **Service Registry & Discovery Server** for the **ChaloKharido** e-commerce microservices platform. Built using **Spring Cloud Netflix Eureka Server**, enabling dynamic service registration, client-side load balancing, and zero-hardcoded-IP communication across all distributed services.

---

## 🌟 Key Responsibilities

- **Centralized Service Registry**: Maintains an active catalog of all running microservice instances with hostnames, ports, and health status.
- **Dynamic Discovery**: Allows services like `api-gateway`, `user-service`, `product-service`, and `order-service` to locate each other dynamically using service names (e.g., `lb://user-service`).
- **Heartbeat & Health Checks**: Monitors microservice instances via periodic 30-second heartbeats, automatically evicting dead instances to prevent traffic blackholes.
- **Standalone Registry Mode**: Configured with `register-with-eureka=false` and `fetch-registry=false` so the server acts purely as the registry hub.

---

## 🏗️ Architecture

```mermaid
flowchart TD
    Eureka[🧭 Eureka Discovery Server :8761]
    
    Gateway[API Gateway :8090] -.->|Locates Services| Eureka
    UserService[User Service :8081] -->|Register & Heartbeat| Eureka
    ProductService[Product Service :8082] -->|Register & Heartbeat| Eureka
    OrderService[Order Service :8083] -->|Register & Heartbeat| Eureka
    PaymentService[Payment Service :8084] -->|Register & Heartbeat| Eureka
    NotificationService[Notification Service :8085] -->|Register & Heartbeat| Eureka
```

---

## ⚙️ Configuration

Configured via `application.properties`:

```properties
spring.application.name=discover-service
server.port=8761

# Standalone Registry Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

---

## 🚀 Running Locally

### Prerequisites
- **JDK 17**
- **Maven** or included Maven Wrapper (`mvnw`)

```bash
# Run with Maven Wrapper
./mvnw clean spring-boot:run
```
*(On Windows: `mvnw.cmd spring-boot:run`)*

### Eureka Dashboard
Once running, open your browser and navigate to:  
👉 **`http://localhost:8761`**  
You will see the Eureka Server dashboard showing all currently connected microservices in real-time.

---

## 🐳 Docker Deployment

```bash
# Build the JAR
./mvnw clean package -DskipTests

# Build Docker Image
docker build -t discover-service:latest .

# Run Container
docker run -d -p 8761:8761 --name discover-service discover-service:latest
```

---

## 👤 Author
* **Suzain Naaz**
* GitHub: [@suzain2024](https://github.com/suzain2024)
