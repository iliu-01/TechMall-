package com.techmall.order.controller;

import com.techmall.common.result.Result;
import com.techmall.order.dto.CreateOrderDTO;
import com.techmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<?> createOrder(@RequestBody CreateOrderDTO dto,
                                 @RequestHeader("X-User-Id") Long userId) {
        return orderService.createOrder(dto, userId);
    }

    @GetMapping("/my")
    public Result<?> getMyOrders(@RequestHeader("X-User-Id") Long userId,
                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        return orderService.getMyOrders(userId, page, size);
    }

    @GetMapping("/{id}")
    public Result<?> getOrderDetail(@PathVariable("id") Long id) {
        return orderService.getOrderDetail(id);
    }

    @GetMapping("/merchant")
    public Result<?> getMerchantOrders(@RequestHeader("X-User-Id") Long userId,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        return orderService.getMerchantOrders(userId, page, size);
    }

    @GetMapping("/list")
    public Result<?> listAllOrders(@RequestParam(name = "page", defaultValue = "1") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                   @RequestParam(name = "status", required = false) String status,
                                   @RequestParam(name = "userId", required = false) Long userId) {
        return orderService.listAllOrders(page, size, status, userId);
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable("id") Long id,
                                  @RequestBody Map<String, String> body,
                                  @RequestHeader("X-User-Id") Long userId) {
        String status = body.get("status");
        return orderService.updateOrderStatus(id, status, userId);
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable("id") Long id,
                                 @RequestHeader("X-User-Id") Long userId) {
        return orderService.cancelOrder(id, userId);
    }

    @PutMapping("/{id}/pay")
    public Result<?> payOrder(@PathVariable("id") Long id,
                              @RequestHeader("X-User-Id") Long userId) {
        return orderService.payOrder(id, userId);
    }

    @GetMapping("/stats")
    public Result<?> stats() {
        return orderService.getStats();
    }
}
