package com.techmall.order.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long productId;
    private Integer quantity;
}
