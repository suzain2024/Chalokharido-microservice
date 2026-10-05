package com.example.order_service.OrderDto;


public class OrderResponseDto {

    private Long orderId;
    private String order_no;
    private Double amount;
    private Integer quantity;
    private Long userId;
    private Long productId;
    private String orderStatus;

    public String getOrder_no() {
        return order_no;
    }

    public void setId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }



    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPrice(Double amount) {
        this.amount =amount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getAmount() {
        return amount;
    }



    public Long getOrderId() {
        return orderId;
    }
}
