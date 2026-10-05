package com.example.order_service.OrderService;

import com.example.order_service.OrderDto.OrderRequestDto;
import com.example.order_service.OrderDto.OrderResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface OrderService {
    public OrderResponseDto createOrder( OrderRequestDto order);

    public OrderResponseDto getOrderById( Long id);

    public List<OrderResponseDto> getAll();

    public OrderResponseDto updateOrder( Long id, OrderRequestDto orderDto);

    public OrderResponseDto deleteOrder( Long id);

}
