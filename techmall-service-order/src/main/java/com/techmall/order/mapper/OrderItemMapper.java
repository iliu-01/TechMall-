package com.techmall.order.mapper;

import com.techmall.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem orderItem);

    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
