package com.example.order_service.OrderController;

import com.example.order_service.OrderDto.OrderRequestDto;
import com.example.order_service.OrderDto.OrderResponseDto;
import com.example.order_service.OrderService.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public OrderResponseDto createOrder(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody OrderRequestDto order) {
        // If needed, order.setUserId(Long.parseLong(userId));
        OrderResponseDto createdOrder = orderService.createOrder(order);
        return createdOrder;
    }

    @GetMapping("/get/{id}")
    public OrderResponseDto getOrderById(@Valid @PathVariable Long id) {
        OrderResponseDto createdOrder = orderService.getOrderById(id);
        return createdOrder;
    }

    @GetMapping
    public List<OrderResponseDto> getAll() {
        List<OrderResponseDto> createdOrder = orderService.getAll();
        return createdOrder;
    }

    @PutMapping("/update/{id}")
    public OrderResponseDto updateOrder(@Valid @PathVariable Long id, @Valid @RequestBody OrderRequestDto orderDto) {
        OrderResponseDto createdOrder = orderService.updateOrder(id, orderDto);
        return createdOrder;
    }

    @DeleteMapping("/delete/{id}")
    public OrderResponseDto deleteOrder(@Valid @PathVariable Long id) {
        OrderResponseDto createdOrder = orderService.deleteOrder(id);
        return createdOrder;
    }
}