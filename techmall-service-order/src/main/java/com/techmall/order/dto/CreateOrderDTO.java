package com.techmall.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDTO {
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;
    private List<OrderItemDTO> items;
}
