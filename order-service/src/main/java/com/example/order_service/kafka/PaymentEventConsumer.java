package com.example.order_service.kafka;

import com.example.order_service.OrderEntity.OrderEntity;
import com.example.order_service.OrderRepository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "payment-events-topic", groupId = "order-group")
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaHandler
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Payment SUCCESS received for Order ID: {}, Payment ID: {}", event.getOrderId(), event.getPaymentId());
        updateOrderStatus(event.getOrderId(), "PAID");
    }

    @KafkaHandler
    public void handlePaymentFailure(PaymentFailureEvent event) {
        log.warn("Payment FAILED received for Order ID: {}. Reason: {}", event.getOrderId(), event.getFailureReason());
        updateOrderStatus(event.getOrderId(), "PAYMENT_FAILED");
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object object) {
        log.info("Received unhandled payment event object: {}", object);
    }

    private void updateOrderStatus(Long orderId, String status) {
        Optional<OrderEntity> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            OrderEntity order = optionalOrder.get();
            order.setOrderStatus(status);
            orderRepository.save(order);
            log.info("Order ID: {} status successfully updated to '{}'", orderId, status);
        } else {
            log.error("Order ID: {} not found in database to update status to '{}'", orderId, status);
        }
    }
}
