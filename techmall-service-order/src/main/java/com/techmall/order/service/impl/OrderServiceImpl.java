package com.techmall.order.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.techmall.common.dto.PageDTO;
import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.Result;
import com.techmall.common.result.ResultCode;
import com.techmall.order.dto.CreateOrderDTO;
import com.techmall.order.dto.OrderItemDTO;
import com.techmall.order.entity.Order;
import com.techmall.order.entity.OrderItem;
import com.techmall.order.feign.ProductFeignClient;
import com.techmall.order.feign.UserFeignClient;
import com.techmall.order.mapper.OrderItemMapper;
import com.techmall.order.mapper.OrderMapper;
import com.techmall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "PAID", "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED");
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "PENDING", Set.of("PAID", "CANCELLED"),
        "PAID", Set.of("SHIPPED", "CANCELLED"),
        "SHIPPED", Set.of("DELIVERED", "COMPLETED"),
        "DELIVERED", Set.of("COMPLETED"),
        "COMPLETED", Set.of(),
        "CANCELLED", Set.of()
    );

    @Override
    @SentinelResource(value = "createOrder", fallback = "createOrderFallback")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> createOrder(CreateOrderDTO dto, Long userId) {
        // 校验 items 非空
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(400, "订单商品不能为空");
        }

        // 1. 遍历 items，逐个调用 productFeignClient.getProduct() 获取价格
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO item : dto.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException(400, "商品数量必须大于0");
            }
            Result<?> pr = productFeignClient.getProduct(item.getProductId());
            if (pr.getCode() != 200) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> pd = (Map<String, Object>) pr.getData();
            String name = (String) pd.get("name");
            BigDecimal price = new BigDecimal(pd.get("price").toString());
            Integer productStock = pd.get("stock") != null
                    ? Integer.valueOf(pd.get("stock").toString()) : 0;
            if (productStock < item.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
            }
            Long merchantId = pd.get("merchantId") != null
                    ? Long.valueOf(pd.get("merchantId").toString()) : 0L;

            OrderItem oi = new OrderItem();
            oi.setProductId(item.getProductId());
            oi.setMerchantId(merchantId);
            oi.setProductName(name);
            oi.setProductPrice(price);
            oi.setQuantity(item.getQuantity());
            oi.setAmount(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            items.add(oi);
            total = total.add(oi.getAmount());
        }
        // 2. 创建订单
        Order order = new Order();
        order.setOrderNo("TM" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddr(dto.getReceiverAddr());
        orderMapper.insert(order);
        // 3. 保存订单项 + 扣库存（检查扣减结果）
        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
            Map<String, Integer> stockBody = new HashMap<>();
            stockBody.put("quantity", oi.getQuantity());
            Result<?> stockResult = productFeignClient.deductStock(oi.getProductId(), stockBody);
            if (stockResult.getCode() != 200) {
                throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
            }
        }
        return Result.success(order);
    }

    public Result<?> createOrderFallback(CreateOrderDTO dto, Long userId, Throwable t) {
        log.error("createOrder fallback triggered, userId={}, error={}", userId, t != null ? t.toString() : "unknown");
        return Result.fail(ResultCode.SERVICE_BUSY);
    }

    @Override
    public Result<?> getMyOrders(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.selectByUserId(userId);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        PageDTO<Order> pageDTO = PageDTO.of(pageInfo.getList(), pageInfo.getTotal(), page, size);
        return Result.success(pageDTO);
    }

    @Override
    public Result<?> getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("items", items);
        return Result.success(result);
    }

    @Override
    public Result<?> getMerchantOrders(Long merchantId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.selectByMerchantId(merchantId);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        PageDTO<Order> pageDTO = PageDTO.of(pageInfo.getList(), pageInfo.getTotal(), page, size);
        return Result.success(pageDTO);
    }

    @Override
    public Result<?> listAllOrders(int page, int size, String status, Long userId) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.selectAll(userId, status);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        PageDTO<Order> pageDTO = PageDTO.of(pageInfo.getList(), pageInfo.getTotal(), page, size);
        return Result.success(pageDTO);
    }

    @Override
    public Result<?> updateOrderStatus(Long orderId, String newStatus, Long userId) {
        if (!VALID_STATUSES.contains(newStatus)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "无效的订单状态: " + newStatus);
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        String currentStatus = order.getStatus();
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // COMPLETED 仅下单用户本人可操作
        if ("COMPLETED".equals(newStatus) && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        orderMapper.updateStatus(orderId, newStatus);
        return Result.success();
    }

    @Override
    public Result<?> cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            return Result.fail(ResultCode.FORBIDDEN);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        orderMapper.updateStatus(orderId, "CANCELLED");
        return Result.success();
    }

    @Override
    public Result<?> payOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 调用用户服务扣减余额
        Map<String, Object> deductBody = new HashMap<>();
        deductBody.put("amount", order.getTotalAmount());
        Result<?> deductResult = userFeignClient.deductBalance(userId, deductBody);
        if (deductResult.getCode() != 200) {
            return Result.fail(400, "余额不足");
        }
        // 款项分配给各商家
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        Map<Long, BigDecimal> merchantAmounts = new HashMap<>();
        for (OrderItem item : items) {
            merchantAmounts.merge(item.getMerchantId(), item.getAmount(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : merchantAmounts.entrySet()) {
            Map<String, Object> addBody = new HashMap<>();
            addBody.put("amount", entry.getValue());
            userFeignClient.addBalance(entry.getKey(), addBody);
        }
        orderMapper.updateStatus(orderId, "PAID");
        return Result.success();
    }

    @Override
    public Result<?> getStats() {
        List<Order> all = orderMapper.selectAll(null, null);
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = all.size();
        BigDecimal totalSales = all.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (String s : VALID_STATUSES) {
            long count = all.stream().filter(o -> s.equals(o.getStatus())).count();
            byStatus.put(s, count);
        }

        Map<Long, BigDecimal> byMerchant = new LinkedHashMap<>();
        // 每个用户的消费/销售额
        Map<Long, BigDecimal> byUser = new LinkedHashMap<>();
        // 每个用户的订单列表
        Map<Long, java.util.List<Map<String, Object>>> userOrders = new LinkedHashMap<>();

        for (Order o : all) {
            if (!"CANCELLED".equals(o.getStatus())) {
                byUser.merge(o.getUserId(), o.getTotalAmount(), BigDecimal::add);

                List<OrderItem> items = orderItemMapper.selectByOrderId(o.getId());
                for (OrderItem item : items) {
                    byMerchant.merge(item.getMerchantId(), item.getAmount(), BigDecimal::add);
                }

                // 保存用户订单摘要
                Map<String, Object> orderSummary = new HashMap<>();
                orderSummary.put("orderId", o.getId());
                orderSummary.put("orderNo", o.getOrderNo());
                orderSummary.put("amount", o.getTotalAmount());
                orderSummary.put("status", o.getStatus());
                orderSummary.put("time", o.getCreatedAt());
                java.util.List<Map<String, Object>> itemsList = new java.util.ArrayList<>();
                for (OrderItem item : items) {
                    Map<String, Object> is = new HashMap<>();
                    is.put("productId", item.getProductId());
                    is.put("productName", item.getProductName());
                    is.put("price", item.getProductPrice());
                    is.put("quantity", item.getQuantity());
                    is.put("amount", item.getAmount());
                    is.put("merchantId", item.getMerchantId());
                    itemsList.add(is);
                }
                orderSummary.put("items", itemsList);
                userOrders.computeIfAbsent(o.getUserId(), k -> new java.util.ArrayList<>()).add(orderSummary);
            }
        }

        List<Order> recent = all.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        stats.put("totalOrders", totalOrders);
        stats.put("totalSales", totalSales);
        stats.put("byStatus", byStatus);
        stats.put("byMerchant", byMerchant);
        stats.put("byUser", byUser);
        stats.put("userOrders", userOrders);
        stats.put("recentOrders", recent);
        return Result.success(stats);
    }
}
