package com.example.payment_service.PaymentService;

import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.PaymentEntity.PaymentStatus;
import com.example.payment_service.PaymentRepository.PaymentRepository;
import com.example.payment_service.kafka.OrderPlacedEvent;
import com.example.payment_service.kafka.PaymentFailureEvent;
import com.example.payment_service.kafka.PaymentSuccessEvent;
import com.example.payment_service.kafka.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    public void processPayment(OrderPlacedEvent orderEvent) {
        log.info("Starting payment processing for Order ID: {}, Amount: {}",
                orderEvent.getOrderId(), orderEvent.getAmount());

        // 1. Idempotency Check (prevent duplicate processing if Kafka retries)
        if (paymentRepository.findByOrderId(orderEvent.getOrderId()).isPresent()) {
            log.warn("Payment for Order ID: {} already exists. Skipping duplicate event.", orderEvent.getOrderId());
            return;
        }

        // 2. Initial State: Create payment record as PENDING
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .amount(orderEvent.getAmount())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        try {
            // 3. Execute Transaction
            executePaymentGatewayCharge(orderEvent);

            // 4. SUCCESS PATH
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .paymentId(payment.getId())
                    .userId(orderEvent.getUserId())
                    .amount(orderEvent.getAmount())
                    .timestamp(LocalTime.now())
                    .build();

            paymentEventProducer.publishPaymentSuccessEvent(successEvent);

        } catch (Exception ex) {
            // 5. FAILURE / CRASH PATH (Saga Compensation Trigger)
            log.error("Payment failed or crashed for Order ID: {}. Error: {}",
                    orderEvent.getOrderId(), ex.getMessage());

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(ex.getMessage());
            paymentRepository.save(payment);

            PaymentFailureEvent failedEvent = PaymentFailureEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .paymentId(payment.getId())
                    .userId(orderEvent.getUserId())
                    .amount(orderEvent.getAmount())
                    .failureReason(ex.getMessage())
                    .timeStamp(LocalTime.now())
                    .build();

            paymentEventProducer.publishPaymentFailedEvent(failedEvent);
        }
    }

    private void executePaymentGatewayCharge(OrderPlacedEvent order) {
        if (order.getAmount().compareTo(new BigDecimal("5000.00")) > 0) {
            throw new RuntimeException("Card Limit Exceeded: Max transaction is $5,000");
        }

        if (order.getAmount().compareTo(new BigDecimal("999.00")) == 0) {
            throw new RuntimeException("Payment Gateway Timeout: 3rd-party banking service unreachable");
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Charge successfully approved for Order ID: {}", order.getOrderId());
    }
}
