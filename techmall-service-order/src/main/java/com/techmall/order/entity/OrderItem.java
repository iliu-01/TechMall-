package com.techmall.order.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long merchantId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal amount;
}
