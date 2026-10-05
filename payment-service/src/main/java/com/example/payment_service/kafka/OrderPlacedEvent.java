package com.example.payment_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private Integer quantity;
    private BigDecimal amount;
}
