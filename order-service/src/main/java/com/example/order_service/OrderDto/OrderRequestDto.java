package com.example.order_service.OrderDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class OrderRequestDto {
@NotNull(message="Product id can't be Null")
    private Long productId;
    @NotNull(message="Quantity can't be Null")
    @Min(message = "Atleast 1 quantity", value = 1)
    private Integer quantity;
    @NotNull(message="User id can't be Null")
    private Long userId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
