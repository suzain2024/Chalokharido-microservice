package com.example.order_service.OrderService;

import com.example.order_service.OrdeClient.ProductClient;
import com.example.order_service.OrderDto.OrderRequestDto;
import com.example.order_service.OrderDto.OrderResponseDto;
import com.example.order_service.OrderDto.ProductResponseDto;
import com.example.order_service.OrderEntity.OrderEntity;
import com.example.order_service.OrderRepository.OrderRepository;
import com.example.order_service.kafka.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public OrderServiceImpl(OrderRepository orderRepository, ProductClient productClient) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    @Override
    public OrderResponseDto createOrder(OrderRequestDto order) {
        // 1. Call product-service via OpenFeign
        ProductResponseDto product = productClient.getProductById(order.getProductId());
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + order.getProductId());
        }

        // 2. Stock Check
        int availableStock = (product.getStock() != null) ? product.getStock() : 0;
        if (availableStock < order.getQuantity()) {
            throw new RuntimeException("Insufficient stock! Available: " + availableStock + ", Requested: " + order.getQuantity());
        }

        // 3. Populate and save Order with initial PENDING_PAYMENT status
        OrderEntity o = new OrderEntity();
        o.setOrder_no(UUID.randomUUID().toString());
        o.setProductId(order.getProductId());
        o.setUserId(order.getUserId());
        o.setQuantity(order.getQuantity());
        o.setPrice(product.getPrice() * order.getQuantity());
        o.setOrderStatus("PENDING_PAYMENT");
        OrderEntity savedOrder = orderRepository.save(o);

        // 4. Publish OrderPlacedEvent to Kafka topic "order-events-topic"
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .quantity(savedOrder.getQuantity())
                .amount(BigDecimal.valueOf(savedOrder.getPrice()))
                .build();
        kafkaTemplate.send("order-events-topic", String.valueOf(savedOrder.getId()), event);

        // 5. Build response
        OrderResponseDto newOrder = new OrderResponseDto();
        newOrder.setId(savedOrder.getId());
        newOrder.setOrder_no(savedOrder.getOrder_no());
        newOrder.setProductId(savedOrder.getProductId());
        newOrder.setUserId(savedOrder.getUserId());
        newOrder.setQuantity(savedOrder.getQuantity());
        newOrder.setPrice(savedOrder.getPrice());
        newOrder.setOrderStatus(savedOrder.getOrderStatus());
        return newOrder;
    }

    public OrderResponseDto createOrderFallback(OrderRequestDto order, Throwable throwable) {
        if (throwable instanceof RuntimeException && throwable.getMessage() != null && throwable.getMessage().startsWith("Insufficient")) {
            throw (RuntimeException) throwable;
        }

        OrderResponseDto fallback = new OrderResponseDto();
        fallback.setUserId(order.getUserId());
        fallback.setProductId(order.getProductId());
        fallback.setQuantity(order.getQuantity());
        fallback.setPrice(0.0);
        fallback.setOrderStatus("FAILED: Product service is temporarily unavailable. Please try again shortly.");
        return fallback;
    }

    public OrderResponseDto getOrderById(Long id) {
        OrderEntity o = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found: " + id));
        OrderResponseDto newOrder = new OrderResponseDto();
        newOrder.setProductId(o.getProductId());
        newOrder.setUserId(o.getUserId());
        newOrder.setQuantity(o.getQuantity());
        newOrder.setPrice(o.getPrice());
        newOrder.setId(o.getId());
        newOrder.setOrder_no(o.getOrder_no());
        newOrder.setOrderStatus(o.getOrderStatus());
        return newOrder;
    }

    public List<OrderResponseDto> getAll() {
        List<OrderResponseDto> orderList = new ArrayList<>();
        List<OrderEntity> ol = orderRepository.findAll();
        for (OrderEntity o : ol) {
            OrderResponseDto order = new OrderResponseDto();
            order.setId(o.getId());
            order.setOrder_no(o.getOrder_no());
            order.setOrderStatus(o.getOrderStatus());
            order.setUserId(o.getUserId());
            order.setProductId(o.getProductId());
            order.setPrice(o.getPrice());
            order.setQuantity(o.getQuantity());
            orderList.add(order);
        }
        return orderList;
    }

    public OrderResponseDto updateOrder(Long id, OrderRequestDto order) {
        OrderEntity o = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Id not found: " + id));
        o.setQuantity(order.getQuantity());
        o.setProductId(order.getProductId());
        o.setUserId(order.getUserId());
        OrderEntity savedOrder = orderRepository.save(o);
        OrderResponseDto newOrder = new OrderResponseDto();
        newOrder.setPrice(savedOrder.getPrice());
        newOrder.setProductId(savedOrder.getProductId());
        newOrder.setUserId(savedOrder.getUserId());
        newOrder.setQuantity(savedOrder.getQuantity());
        newOrder.setOrder_no(savedOrder.getOrder_no());
        newOrder.setOrderStatus(savedOrder.getOrderStatus());
        newOrder.setId(savedOrder.getId());
        return newOrder;
    }

    public OrderResponseDto deleteOrder(Long id) {
        OrderEntity o = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Id not found: " + id));
        orderRepository.delete(o);
        OrderResponseDto newOrder = new OrderResponseDto();
        newOrder.setPrice(o.getPrice());
        newOrder.setProductId(o.getProductId());
        newOrder.setUserId(o.getUserId());
        newOrder.setQuantity(o.getQuantity());
        newOrder.setOrder_no(o.getOrder_no());
        newOrder.setOrderStatus(o.getOrderStatus());
        newOrder.setId(o.getId());
        return newOrder;
    }
}
