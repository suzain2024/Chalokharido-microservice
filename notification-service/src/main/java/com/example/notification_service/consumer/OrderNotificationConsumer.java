package com.example.notification_service.consumer;

import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class OrderNotificationConsumer {

    @KafkaListener(topics = "order-events-topic", groupId = "notification-order-group")
    public void consumeOrderPlaced(String message) {
        System.out.println("[NOTIFICATION SERVICE] Order Placed Event: " + message);
        System.out.println("--> Simulating: Sending Order Confirmation Email to customer...");
    }

    @KafkaListener(topics = "payment-events-topic", groupId = "notification-payment-group")
    public void consumePaymentResult(String message) {
        System.out.println("[NOTIFICATION SERVICE] Payment Status Event: " + message);
        System.out.println("--> Simulating: Sending Payment Receipt to customer...");
    }
}