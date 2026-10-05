package com.example.payment_service.kafka;

import com.example.payment_service.PaymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events-topic", groupId = "payment-group")
    public void consumeOrder(OrderPlacedEvent event) {
        log.info("Kafka received OrderPlacedEvent for order ID: {}", event.getOrderId());
        paymentService.processPayment(event);
    }
}
