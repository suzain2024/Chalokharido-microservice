package com.example.payment_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events-topic";
    public final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentFailedEvent(PaymentFailureEvent event) {
        log.warn("Emitting PaymentFailedEvent for Order ID: {} | Reason: {}",
                event.getOrderId(), event.getFailureReason());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(event.getOrderId()), event);
    }

    public void publishPaymentSuccessEvent(PaymentSuccessEvent event) {
        log.info("Emitting PaymentSuccessEvent for Order ID: {}", event.getOrderId());
        kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, String.valueOf(event.getOrderId()), event);
    }
}