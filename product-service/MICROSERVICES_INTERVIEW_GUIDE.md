# Complete Microservices & Security Interview Master Guide
> **A Comprehensive Reference Guide for Microservices Architecture, Spring Cloud, Resilience, Security (CORS & JWT), and Interview Preparation.**

---

## Table of Contents
1. [Architecture Overview & System Design](#1-architecture-overview--system-design)
2. [Our Domain Services (What We Built)](#2-our-domain-services-what-we-built)
3. [Service Registry & Discovery (Netflix Eureka)](#3-service-registry--discovery-netflix-eureka)
4. [Inter-Service Communication (Spring Cloud OpenFeign)](#4-inter-service-communication-spring-cloud-openfeign)
5. [Client-Side Load Balancing (Spring Cloud LoadBalancer)](#5-client-side-load-balancing-spring-cloud-loadbalancer)
6. [Fault Tolerance & Circuit Breakers (Resilience4j)](#6-fault-tolerance--circuit-breakers-resilience4j)
7. [API Gateway (Spring Cloud Gateway)](#7-api-gateway-spring-cloud-gateway)
8. [Security Deep Dive: CORS & JWT Authentication](#8-security-deep-dive-cors--jwt-authentication)
9. [Advanced Production Concepts (What Makes You a Senior Dev)](#9-advanced-production-concepts-what-makes-you-a-senior-dev)
10. [Top 20 High-Yield Interview Questions & Model Answers](#10-top-20-high-yield-interview-questions--model-answers)

---

## 1. Architecture Overview & System Design

In a monolithic architecture, everything (User, Product, Order, Payment) runs inside a single JVM process and shares a single database. If one module has a memory leak, the entire application crashes.

In our **Microservices Architecture**, each domain is an independent, independently deployable service with its own dedicated database (the **Database-per-Service pattern**):

```
                                    CLIENT (Web / Mobile / Postman)
                                                   │
                                                   ▼ (Port 8080)
                              ┌─────────────────────────────────────────┐
                              │           API GATEWAY (Port 8080)       │
                              │  - Single Entry Point                   │
                              │  - CORS Management                      │
                              │  - JWT Verification (The "Bouncer")     │
                              │  - Rate Limiting & Routing              │
                              └────────────────────┬────────────────────┘
                                                   │
                ┌──────────────────────────────────┼──────────────────────────────────┐
                ▼                                  ▼                                  ▼
      ┌───────────────────┐              ┌───────────────────┐              ┌───────────────────┐
      │   user-service    │              │  product-service  │              │   order-service   │
      │    (Port 8081)    │              │    (Port 8082)    │              │    (Port 8083)    │
      │  - User profiles  │              │  - Catalog/Price  │              │  - Creates Orders │
      │  - Auth / JWT     │              │  - Stock balance  │              │  - Calls Product  │
      └─────────┬─────────┘              └─────────┬─────────┘              └─────────┬─────────┘
                │                                  │                                  │
                │        OpenFeign Synchronous     │        OpenFeign Synchronous     │
                └──────────────◄───────────────────┴──────────────◄───────────────────┘
                                                   │
                                                   ▼ (Heartbeats & Registry)
                                  ┌─────────────────────────────────┐
                                  │   Eureka Discovery Server       │
                                  │          (Port 8761)            │
                                  │   - Service Registry            │
                                  │   - Dynamic IP & Port Tracking  │
                                  └─────────────────────────────────┘
                                                   │
                                     (Event-Driven / Asynchronous)
                                                   ▼
                                  ┌─────────────────────────────────┐
                                  │     Apache Kafka / RabbitMQ     │
                                  └────────────────┬────────────────┘
                                                   │
                         ┌─────────────────────────┴─────────────────────────┐
                         ▼                                                   ▼
              ┌─────────────────────┐                             ┌─────────────────────┐
              │   payment-service   │                             │ notification-service│
              │     (Port 8084)     │                             │     (Port 8085)     │
              │  - Process Charges  │                             │  - Emails / SMS     │
              └─────────────────────┘                             └─────────────────────┘
```

---

## 2. Our Domain Services (What We Built)

### 1. `user-service` (Port 8081)
* **Responsibility**: Manages user profiles, credentials, registration, and profile updates.
* **Core Entity**: `UserEntity` (`id`, `name`, `email`).
* **Endpoints**:
  * `POST /api/user/create` &rarr; Register a user.
  * `GET /api/user/get/{id}` &rarr; Fetch user by ID.
  * `GET /api/user/get` &rarr; Fetch all users.
  * `PUT /api/user/update/{id}` &rarr; Update profile.
  * `DELETE /api/user/delete/{id}` &rarr; Delete user.

### 2. `product-service` (Port 8082)
* **Responsibility**: Manages inventory catalog, pricing, and available stock.
* **Core Entity**: `ProductEntity` (`id`, `name`, `price`, `stock`).
* **Endpoints**:
  * `POST /api/product/create` &rarr; Add new product.
  * `GET /api/product/get/{id}` &rarr; Query price & stock.
  * `GET /api/product/get` &rarr; List products.
  * `PUT /api/product/update/{id}` &rarr; Update price/stock.
  * `DELETE /api/product/delete/{id}` &rarr; Remove product.

### 3. `order-service` (Port 8083)
* **Responsibility**: Connects User and Product to execute customer purchases.
* **Core Entity**: `OrderEntity` (`id`, `order_no`, `userId`, `productId`, `quantity`, `price`, `orderStatus`).
* **Key Collaboration**:
  * Calls `user-service` to verify user existence.
  * Calls `product-service` to check stock availability and fetch accurate unit price.
  * Calculates `totalPrice = product.price * quantity`.

---

## 3. Service Registry & Discovery (Netflix Eureka)

### What is it?
Eureka is the **Phonebook / Yellow Pages** of your microservices ecosystem. It consists of two components:
1. **Eureka Server**: A standalone application holding a registry table of all service names and their network locations (IP + Port).
2. **Eureka Client**: A dependency included in every microservice that registers itself upon startup and sends periodic heartbeats.

### Why is it needed?
* **Dynamic IP & Port Allocation**: In cloud environments (Kubernetes, AWS, Docker), containers frequently restart and receive dynamic IP addresses. You cannot hardcode `http://localhost:8082`.
* **Auto-Scaling**: If traffic spikes and you spin up 5 instances of `product-service`, Eureka automatically tracks all 5 instances.
* **Health Tracking**: Clients send a heartbeat every 30 seconds. If a service dies, Eureka evicts it from the registry so traffic isn't routed to a dead server.

### How it is Implemented:

#### Step 1: Create `discovery-service` (Eureka Server)
* **`pom.xml`**:
  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
  </dependency>
  ```
* **Main Class**:
  ```java
  @SpringBootApplication
  @EnableEurekaServer // <-- Activates Eureka Registry
  public class DiscoveryServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(DiscoveryServiceApplication.class, args);
      }
  }
  ```
* **`application.properties`**:
  ```properties
  server.port=8761
  spring.application.name=discovery-service

  # Don't register Eureka with itself:
  eureka.client.register-with-eureka=false
  eureka.client.fetch-registry=false
  ```

#### Step 2: Register Microservices as Eureka Clients
* **In each service's `pom.xml`**:
  ```xml
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
  ```
* **In each service's `application.properties`**:
  ```properties
  spring.application.name=product-service
  server.port=8082
  eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
  ```

---

## 4. Inter-Service Communication (Spring Cloud OpenFeign)

### What is it?
OpenFeign is a **declarative HTTP client**. Instead of writing boilerplate code using `RestTemplate`, `HttpClient`, or `WebClient`, you define an interface annotated with Spring MVC annotations. Feign automatically generates the HTTP request execution at runtime.

### Why is it needed?
| Feature | `RestTemplate` | `WebClient` | `OpenFeign` |
| :--- | :--- | :--- | :--- |
| **Style** | Imperative / Manual | Reactive / Non-blocking | Declarative Interface |
| **Boilerplate** | High (build URLs, parse JSON) | Medium (reactive chains) | **Zero** (just an interface) |
| **Eureka Integration** | Manual config required | Supported | **Built-in natively** |
| **Load Balancing** | Requires `@LoadBalanced` | Requires config | **Automatic** |

### How it is Implemented:

#### In `order-service`:
1. **`pom.xml`**:
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ```
2. **Main Application**:
   ```java
   @SpringBootApplication
   @EnableFeignClients // <-- Enables Feign scanning
   public class OrderServiceApplication { ... }
   ```
3. **Declare Client Interface**:
   ```java
   package com.example.order_service.client;

   import com.example.order_service.OrderDto.ProductResponseDto;
   import org.springframework.cloud.openfeign.FeignClient;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;

   // Name matches the spring.application.name in Eureka!
   @FeignClient(name = "product-service")
   public interface ProductClient {

       @GetMapping("/api/product/get/{id}")
       ProductResponseDto getProductById(@PathVariable("id") Long id);
   }
   ```
4. **Use in `OrderServiceImpl`**:
   ```java
   @Service
   public class OrderServiceImpl implements OrderService {
       private final OrderRepository orderRepository;
       private final ProductClient productClient;

       public OrderServiceImpl(OrderRepository orderRepository, ProductClient productClient) {
           this.orderRepository = orderRepository;
           this.productClient = productClient;
       }

       @Override
       public OrderResponseDto createOrder(OrderRequestDto order) {
           // Feign calls product-service over the network transparently!
           ProductResponseDto product = productClient.getProductById(order.getProductId());

           if (product.getStock() < order.getQuantity()) {
               throw new RuntimeException("Insufficient stock!");
           }
           ...
       }
   }
   ```

---

## 5. Client-Side Load Balancing (Spring Cloud LoadBalancer)

### What is it?
In traditional server-side load balancing (like NGINX or AWS ALB), the client sends a request to a proxy hardware/server, which then forwards it to one of the backend servers.

In **Client-Side Load Balancing** (Spring Cloud LoadBalancer):
* The client (`order-service`) asks Eureka for all available instances of `product-service`.
* Eureka returns a list: `[192.168.1.10:8082, 192.168.1.11:8082, 192.168.1.12:8082]`.
* **The client itself** decides which instance to call using an algorithm like **Round-Robin** or **Random**.

```
Request 1 ──► Instance A (Port 8082)
Request 2 ──► Instance B (Port 8086)
Request 3 ──► Instance C (Port 8087)
Request 4 ──► Instance A (Port 8082)
```

### Why is it needed?
* **No Single Point of Failure (SPOF)**: Eliminates an extra load-balancer hardware hop between internal microservices.
* **Lower Latency**: `order-service` connects directly to the target `product-service` instance.
* **Built Right into OpenFeign**: Spring Cloud automatically applies LoadBalancer to every `@FeignClient(name = "product-service")`.

---

## 6. Fault Tolerance & Circuit Breakers (Resilience4j)

### What is it?
A design pattern modeled after the electrical circuit breaker in your house. If a downstream microservice is down or unresponsive, the circuit breaker trips open to protect the calling service from crashing.

### The 3 Circuit Breaker States:

```
      [ 50% Failures Occur ]
   ┌───────────────────────────┐
   ▼                           │
┌──────────────┐         ┌────────────┐
│    CLOSED    │         │    OPEN    │
│ (Normal Ops) │         │ (Tripped!) │
└──────────────┘         └─────┬──────┘
   ▲                           │
   │  [ Trial Requests Pass ]  │ [ Sleep Window Expires ]
   └───────────┌───────────────┘
               ▼
        ┌─────────────┐
        │  HALF-OPEN  │
        └─────────────┘
```

1. **CLOSED (Normal)**: All requests flow directly to `product-service`. Everything is healthy.
2. **OPEN (Tripped)**: If failure rate exceeds a threshold (e.g. 50% errors over 10 calls), the circuit trips. For the next 30 seconds, **no network calls are made to `product-service`**. Requests immediately jump to the **Fallback Method**.
3. **HALF-OPEN (Testing)**: After the wait duration expires, the breaker lets a small number of trial requests through. If they succeed, it returns to **CLOSED**; if they fail, it resets back to **OPEN**.

### Why is it needed?
* **Prevents Cascading Failures**: When `product-service` hangs for 30s, threads in `order-service` pile up waiting. Without a circuit breaker, `order-service` exhausts its thread pool and crashes too.
* **Graceful Degradation**: Instead of showing an HTTP 500 error page, you return a helpful cached or fallback response.

### How it is Implemented:

1. **Add Dependency**:
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
   </dependency>
   ```
2. **Apply `@CircuitBreaker` in Service**:
   ```java
   @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
   public ProductResponseDto fetchProductDetails(Long productId) {
       return productClient.getProductById(productId);
   }

   // Fallback must have identical return type and take Throwable as last parameter:
   public ProductResponseDto productFallback(Long productId, Throwable throwable) {
       ProductResponseDto fallback = new ProductResponseDto();
       fallback.setId(String.valueOf(productId));
       fallback.setName("Temporary Unavailable Product");
       fallback.setPrice(0.0);
       fallback.setStock(0);
       return fallback;
   }
   ```

---

## 7. API Gateway (Spring Cloud Gateway)

### What is it?
The **API Gateway** is the single entry point (reverse proxy) for all incoming client requests (React, Angular, iOS, Android, Postman). It sits between external clients and internal microservices.

### Why is it needed?
1. **Single Entry URL**: Clients only ever talk to port `8080`. They don't need to know or track 10 different ports.
2. **Centralized Security**: Inspect JWT tokens once at the gateway; reject unauthorized users before they touch backend services.
3. **CORS Centralization**: Solve browser cross-origin issues in one place.
4. **Rate Limiting & Throttling**: Block malicious IPs or restrict API abuse (e.g., max 100 requests/minute per user).
5. **Observability & Logging**: Log every incoming request and execution time in one central spot.

### How it is Implemented:

1. **`pom.xml`**:
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-gateway</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```
2. **`application.yml`**:
   ```yaml
   server:
     port: 8080

   spring:
     application:
       name: api-gateway
     cloud:
       gateway:
         discovery:
           locator:
             enabled: true # Automatically discover services from Eureka
         routes:
           - id: user-service-route
             uri: lb://user-service       # 'lb://' uses client-side load balancer!
             predicates:
               - Path=/api/user/**

           - id: product-service-route
             uri: lb://product-service
             predicates:
               - Path=/api/product/**

           - id: order-service-route
             uri: lb://order-service
             predicates:
               - Path=/api/order/**
   ```

---

## 8. Security Deep Dive: CORS & JWT Authentication

### A. The CORS Headache Demystified

#### What is CORS?
Browsers enforce the **Same-Origin Policy**: code loaded from Origin A (`http://localhost:3000`) cannot access data from Origin B (`http://localhost:8081`) unless Origin B explicitly returns the header:
```http
Access-Control-Allow-Origin: http://localhost:3000
```

#### Why it's a Headache in Microservices:
Without an API Gateway:
* A modern browser sends an invisible pre-flight **`OPTIONS`** HTTP request before any `POST`, `PUT`, or `DELETE`.
* You must configure CORS beans in `user-service`, `product-service`, `order-service`, `payment-service`.
* Any discrepancy in headers (`Authorization`, `Content-Type`), credentials, or allowed origins causes client requests to fail silently in the browser.

#### The Gateway Solution (Solve it Once):
With an API Gateway, configure CORS in `api-gateway/application.yml`:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```
All backend microservices run safely in your private network with zero CORS configuration required!

---

### B. JWT Authentication in Microservices

#### What is a JWT?
A JSON Web Token is a compact, URL-safe means of representing claims between two parties. It is composed of three base64-encoded strings separated by dots:
```
header.payload.signature
```

1. **Header (Metadata)**:
   ```json
   { "alg": "HS256", "typ": "JWT" }
   ```
   * *Base64Url Encoded*: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9`
   * *Note*: Readable by anyone; not encrypted.

2. **Payload (Claims / User Details)**:
   ```json
   {
     "sub": "suzain@gmail.com",
     "userId": 101,
     "role": "CUSTOMER",
     "exp": 1758299000
   }
   ```
   * *Base64Url Encoded*: `eyJzdWIiOiJzdXphaW5AZ21haWwuY29tIiwidXNlcklkIjoxMDEsInJvbGUiOiJDVVNUT01FUiIsImV4cCI6MTc1ODI5OTAwMH0`
   * *Note*: Readable by anyone. **Never store sensitive data (passwords, credit cards) here!**

3. **Signature (Cryptographic Seal)**:
   Calculated using HMAC-SHA256 hashing combining the Encoded Header, Encoded Payload, and the shared **Secret Key**:
   $$\text{Signature} = \text{HMAC-SHA256}\Big(\text{EncodedHeader} + \text{"."} + \text{EncodedPayload}, \ \text{SecretKey}\Big)$$
   * Result: `5dK8zL_9xM2v7QwRtYuIoP3kL...`

---

#### 5. End-to-End Chronological Lifecycle

```
+───────────────────────────────────────────────────────────────────────────────────────────+
│                               COMPLETE JWT LIFECYCLE                                      │
+───────────────────────────────────────────────────────────────────────────────────────────+

  [ PHASE 0: APPLICATION CONFIGURATION (Developer Setup) ]
  • Developer generates a 256-bit Secret Key once.
  • Configured in application.yml of user-service (for signing) and api-gateway (for verifying):
    jwt.secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  • Remains identical across all users; secret to backend servers only.

  [ PHASE 1: ACCOUNT REGISTRATION ]
  Client ────────► POST /api/user/register { "name": "Suzain", "email": "...", "password": "123" }
                          │
                          ▼
                   [ user-service ]
                   • Hashes password via BCrypt: "123" ──► "$2a$10$92IXUN..."
                   • Stores record in user_db (id=101, email, hashed_password, role).
                   • (No JWT generated yet!)

  [ PHASE 2: LOGIN & TOKEN ISSUANCE ]
  Client ────────► POST /api/user/login { "email": "...", "password": "123" }
                          │
                          ▼
                   [ user-service ]
                   • Finds user by email in user_db.
                   • Validates password with passwordEncoder.matches().
                   • Builds Header + Payload { userId: 101, role: "CUSTOMER", exp: +24h }.
                   • Calculates Signature using Secret Key via HMAC-SHA256.
                   • Concatenates: Header.Payload.Signature
                          │
  Client ◄────────────────┴── Returns { "token": "eyJhbGciOi..." }

  [ PHASE 3: AUTHENTICATED REQUEST & GATEWAY VERIFICATION ]
  Client ────────► POST /api/order/create
                   Headers: [ Authorization: Bearer eyJhbGciOi... ]
                          │
                          ▼
                   [ API GATEWAY (AuthenticationFilter) ]
                   1. Intercepts request via RouteValidator (checks if route is secured).
                   2. Splits token into Header, Payload, and Signature.
                   3. Re-computes signature: HMAC-SHA256(Header + "." + Payload, SecretKey).
                   4. Compares Re-computed Signature == Token's Signature.
                      - If Mismatch: Rejects immediately (401 Unauthorized).
                      - If Tampered (hacker modified payload): Signature mismatch -> 401.
                   5. Checks expiration (exp > System.currentTimeMillis()).
                   6. Extracts claims (userId = 101, role = "CUSTOMER").
                   7. Mutates HTTP request by adding internal headers:
                      X-User-Id: 101
                      X-User-Role: CUSTOMER
                          │
                          ▼ (Routed downstream)
                   [ order-service / product-service ]
                   • Reads @RequestHeader("X-User-Id") Long userId directly!
                   • Zero authentication code, zero DB overhead, 100% secure.
```

---

#### 6. Code Implementations

##### A. `user-service` Token Generator (`JwtService.java`)
```java
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h validity
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

##### B. `api-gateway` Route Validator & Authentication Filter
```java
@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/api/user/register",
            "/api/user/login",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
}
```

```java
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String token = authHeader.substring(7);

                try {
                    // 1. Verify cryptographic signature & expiry
                    Claims claims = jwtUtil.extractAllClaims(token);

                    // 2. Mutate request headers to propagate user identity downstream
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", String.valueOf(claims.get("userId")))
                            .header("X-User-Role", String.valueOf(claims.get("role")))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        };
    }
}
```

##### C. Downstream Services (`product-service` / `order-service`)
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody OrderRequestDto requestDto) {
        // userId is verified and trusted directly from the Gateway!
        return new ResponseEntity<>(orderService.createOrder(userId, requestDto), HttpStatus.CREATED);
    }
}
```

---

#### 7. Security Best Practices for Production

1. **Environment Variables**: Never commit raw Secret Keys to Git repositories. Supply them dynamically at runtime:
   ```yaml
   jwt:
     secret: ${JWT_SECRET_KEY}
   ```
2. **Key Length**: Use at least 256-bit (64 hex characters) keys to prevent brute-force attacks.
3. **Token Expiration & Refresh Tokens**: Set short access token lifespans (15–60 mins) combined with rotating Refresh Tokens stored securely in HTTP-Only cookies.
4. **Asymmetric Signing (RS256)**: For multi-organization or large enterprise architectures, use a **Private Key** at `user-service` to sign tokens and a **Public Key** at `api-gateway` to verify them.

---

## 9. Advanced Production Concepts (What Makes You a Senior Dev)

### 1. Distributed Tracing (Micrometer Tracing + Zipkin)
* **The Problem**: A request to `/api/order/create` fails with an HTTP 500 error. The request went through: `Gateway ──► Order ──► Product ──► Payment`. **Which service failed?**
* **The Solution**:
  * **`TraceId`**: A single ID assigned to the request at the Gateway and propagated to every service in HTTP headers (`traceparent`).
  * **`SpanId`**: Represents an individual hop or work unit within a service.
  * **Zipkin Dashboard**: Paste the `TraceId` and see a timeline showing the exact failure line and duration of every service.

### 2. Centralized Configuration (Spring Cloud Config Server)
* **The Problem**: Changing a database URL, logging level, or secret key across 10 microservices requires rebuilding and redeploying 10 JAR files.
* **The Solution**: Store all `application.yml` files in a centralized Git repo. A `config-server` serves these configurations dynamically. Using `@RefreshScope`, services update their configuration on the fly without restarting!

### 3. Event-Driven Messaging (Apache Kafka / RabbitMQ)
* **Synchronous (OpenFeign)**: Used when the caller **needs the response immediately to make a decision** (e.g. `order-service` checking product stock).
* **Asynchronous (Kafka)**: Used when the caller **doesn't need to wait** (e.g. sending a confirmation email or generating an invoice PDF).
  * `order-service` saves the order, pushes an `OrderPlacedEvent` to Kafka, and immediately responds to the user: `"Order Placed!"`.
  * `notification-service` consumes the event in the background and sends the email at its own pace.

### 4. Distributed Transactions: The Saga Pattern
* **The Problem**: You cannot use `@Transactional` across three separate databases (`order-db`, `product-db`, `payment-db`).
* **The Solution (Compensating Transactions)**:
  1. `order-service` creates order in status `"PENDING"`.
  2. `payment-service` tries to charge credit card.
  3. If payment fails: It publishes `PaymentFailedEvent`.
  4. `order-service` catches event and runs a **Compensating Transaction**: sets order status to `"CANCELLED"` and calls `product-service` to restore inventory.

---

## 10. Top 20 High-Yield Interview Questions & Model Answers

#### Q1: What is the difference between a Monolith and a Microservices Architecture?
> **Answer**: A monolith bundles UI, business logic, and database access into a single deployable artifact sharing one database. Microservices decompose an application into small, autonomous, loosely coupled services organized around business capabilities, each maintaining its own database and communicating via lightweight protocols (REST or messaging).

#### Q2: What is the "Database-per-Service" pattern and why is it mandatory?
> **Answer**: Each microservice must own its private database. Other services can only access this data through public APIs. This ensures loose coupling, allows independent schema migrations, and prevents one service's query locks from impacting other services.

#### Q3: How does Eureka Service Discovery work?
> **Answer**: Services register with Eureka Server on startup by providing their application name and network coordinates (IP/port). They send heartbeats every 30 seconds to maintain registration. When Service A needs to call Service B, it fetches Service B's instance list from Eureka and caches it locally.

#### Q4: What happens if Eureka Server goes down?
> **Answer**: Microservices cache the registry locally. Even if Eureka goes down, services can continue communicating with each other using their cached IP lists. However, newly spawned instances cannot register until Eureka recovers.

#### Q5: What is Eureka "Self-Preservation Mode"?
> **Answer**: If Eureka Server suddenly stops receiving heartbeats from more than 15% of instances (often due to a network glitch, not actual service failure), it enters Self-Preservation mode. It stops evicting instances from the registry to prevent wiping out healthy services during a transient network partition.

#### Q6: Why use OpenFeign instead of RestTemplate or WebClient?
> **Answer**: OpenFeign is declarative: you write only a Java interface with annotations, eliminating HTTP client boilerplate. It integrates out-of-the-box with Eureka for service discovery and Spring Cloud LoadBalancer for round-robin load balancing.

#### Q7: What is the difference between Client-Side and Server-Side Load Balancing?
> **Answer**: In server-side load balancing (e.g. AWS ALB/NGINX), requests pass through a centralized proxy server. In client-side load balancing (Spring Cloud LoadBalancer), the calling service fetches the list of available instances from Eureka and directly picks one using an algorithm like Round-Robin, eliminating a middleman hop.

#### Q8: What is a Circuit Breaker and what problem does it solve?
> **Answer**: It prevents cascading failures. If downstream Service B fails or experiences extreme latency, the circuit breaker trips from CLOSED to OPEN. Subsequent calls fail immediately or invoke a fallback method without waiting for a timeout, preserving caller threads and resources.

#### Q9: Explain the states of a Circuit Breaker in Resilience4j.
> **Answer**:
> * **CLOSED**: Normal state; requests flow to downstream service.
> * **OPEN**: Failure threshold exceeded; requests fail immediately to fallback.
> * **HALF-OPEN**: Wait duration expires; a limited number of trial calls test if the downstream service has recovered.

#### Q10: What is an API Gateway and why is it essential?
> **Answer**: An API Gateway acts as the single reverse proxy front door for external clients. It handles cross-cutting concerns: dynamic routing to internal services via Eureka, SSL termination, CORS, rate limiting, and centralized JWT authentication.

#### Q11: How do you handle CORS in a Microservices architecture?
> **Answer**: Instead of configuring CORS in every downstream service, handle it globally at the API Gateway using Spring Cloud Gateway's `globalcors` configuration. The Gateway handles the pre-flight `OPTIONS` requests and attaches `Access-Control-Allow-*` headers.

#### Q12: How does JWT authentication work with an API Gateway?
> **Answer**: The client logs in through the `user-service` and receives a signed JWT. On subsequent requests, the client passes `Authorization: Bearer <token>` to the API Gateway. The Gateway's security filter validates the signature and expiration, extracts user claims (like `userId`), and forwards the request to downstream services with an internal header (`X-User-Id`).

#### Q13: Why shouldn't internal microservices verify passwords or database sessions?
> **Answer**: Verifying credentials in every service introduces tight coupling, duplicates security code, and creates massive database bottlenecks. Decoupling auth at the Gateway keeps domain services stateless and focused purely on business logic.

#### Q14: When should you use Synchronous vs Asynchronous communication?
> **Answer**:
> * **Synchronous (REST / Feign)**: When the caller cannot proceed without the immediate response (e.g., `order-service` verifying stock with `product-service`).
> * **Asynchronous (Kafka / RabbitMQ)**: When the caller does not depend on immediate execution (e.g., sending emails, analytics tracking, generating PDFs).

#### Q15: Why can't we use `@Transactional` across microservices?
> **Answer**: `@Transactional` relies on local database ACID transactions (or 2-Phase Commit protocols) bound to a single database connection. In microservices, each service has its own independent database, meaning transactions cannot span across process boundaries.

#### Q16: What is the Saga Pattern?
> **Answer**: A sequence of local transactions where each microservice updates its own database and publishes an event. If a step fails, the Saga executes **Compensating Transactions** backward to undo previous changes (e.g., cancelling an order if payment fails).

#### Q17: What is the difference between Choreography and Orchestration Saga?
> **Answer**:
> * **Choreography**: Event-driven; each service listens to events and decides when to act without a central coordinator.
> * **Orchestration**: A central Saga Orchestrator service tells each participant service what local transaction to execute.

#### Q18: What is Distributed Tracing and what are Trace IDs and Span IDs?
> **Answer**: Distributed tracing tracks the complete lifecycle of a request as it hops across multiple microservices. A **TraceId** uniquely identifies the entire end-to-end transaction. A **SpanId** identifies work done within a specific microservice hop. Tools like Zipkin visualize this call tree.

#### Q19: What is Spring Cloud Config Server?
> **Answer**: A centralized configuration management server that stores environment-specific configuration files in a version-controlled repository (like Git). Microservices fetch their properties at startup, and `@RefreshScope` enables updating configurations without redeployment.

#### Q20: How do you handle database migrations across microservices?
> **Answer**: Use migration tools like **Flyway** or **Liquibase** embedded within each microservice project. When a service boots up, it automatically applies versioned SQL migration scripts to its own database schema independently.

---

### Quick Architecture Cheat-Sheet

| Component | Default Port | Primary Dependency | Key Annotation |
| :--- | :---: | :--- | :--- |
| **Eureka Server** | `8761` | `spring-cloud-starter-netflix-eureka-server` | `@EnableEurekaServer` |
| **API Gateway** | `8080` | `spring-cloud-starter-gateway` | *(Config-driven routes)* |
| **User Service** | `8081` | `spring-cloud-starter-netflix-eureka-client` | `@SpringBootApplication` |
| **Product Service** | `8082` | `spring-cloud-starter-netflix-eureka-client` | `@SpringBootApplication` |
| **Order Service** | `8083` | `spring-cloud-starter-openfeign` | `@EnableFeignClients` |
| **Circuit Breaker** | N/A | `spring-cloud-starter-circuitbreaker-resilience4j` | `@CircuitBreaker` |
