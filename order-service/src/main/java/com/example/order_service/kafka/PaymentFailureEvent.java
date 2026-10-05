package com.example.order_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFailureEvent {
    private Long userId;
    private Long paymentId;
    private String failureReason;
    private BigDecimal amount;
    private Long orderId;
    private LocalTime timeStamp;
}
