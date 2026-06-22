package com.techmall.order.service;

import com.techmall.common.result.Result;
import com.techmall.order.dto.CreateOrderDTO;

public interface OrderService {
    Result<?> createOrder(CreateOrderDTO dto, Long userId);

    Result<?> getMyOrders(Long userId, int page, int size);

    Result<?> getOrderDetail(Long orderId);

    Result<?> getMerchantOrders(Long merchantId, int page, int size);

    Result<?> listAllOrders(int page, int size, String status, Long userId);

    Result<?> updateOrderStatus(Long orderId, String newStatus, Long userId);

    Result<?> cancelOrder(Long orderId, Long userId);

    Result<?> payOrder(Long orderId, Long userId);

    Result<?> getStats();
}
