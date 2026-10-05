# 📦 Product Service — ChaloKharido Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg?logo=redis)](https://redis.io/)
[![Netflix Eureka](https://img.shields.io/badge/Eureka-Client-red.svg)](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker)](https://www.docker.com/)

Product catalog and inventory management microservice for the **ChaloKharido** e-commerce platform. Built with **Spring Boot 3**, **PostgreSQL** (`product_db`), and **Redis Cache-Aside Pattern** on port `8082`.

---

## 🌟 Key Features

- **🛍️ Product Catalog & Stock Management**: Endpoints for creating, updating, listing, and querying product inventory.
- **⚡ Redis Cache-Aside (`@Cacheable`)**: High-throughput read operations cached in Redis with a 10-minute TTL (`600000ms`) to drastically reduce PostgreSQL read load.
- **📦 Database-per-Service**: Independent schema `product_db` ensuring inventory and catalog data are completely decoupled from order and user databases.
- **🔍 Eureka Service Discovery**: Automatically registers with Eureka on port `8761` for client-side discovery and routing via API Gateway.
- **🐳 Containerized**: Dockerized with Alpine JRE for lightweight deployment.

---

## 🏗️ Architecture

```mermaid
flowchart LR
    Gateway[API Gateway :8090] -->|/api/product/**| ProductService[Product Service :8082]
    ProductService -->|Read / Write CRUD| DB[(PostgreSQL\nproduct_db :5432)]
    ProductService -->|Cache Hit / Miss| Cache[(Redis Cache\nTTL: 10m :6379)]
    ProductService -.->|Register| Eureka[Eureka Server :8761]
```

---

## ⚙️ Configuration

Configured via `application.properties`:

```properties
spring.application.name=product-service
server.port=8082

spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000

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
docker build -t product-service:latest .

# Run Container
docker run -d -p 8082:8082 --name product-service product-service:latest
```

---

## 👤 Author
* **Suzain Naaz**
* GitHub: [@suzain2024](https://github.com/suzain2024)
