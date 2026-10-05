package com.example.payment_service.kafka;

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
public class PaymentSuccessEvent {
  private  Long userId;
  private Long orderId;
  private Long paymentId;
  private BigDecimal amount;
  private LocalTime timestamp;
}
