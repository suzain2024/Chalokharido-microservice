package com.example.payment_service.PaymentService;

import com.example.payment_service.kafka.OrderPlacedEvent;

public interface PaymentService {
    void processPayment(OrderPlacedEvent event);
}
