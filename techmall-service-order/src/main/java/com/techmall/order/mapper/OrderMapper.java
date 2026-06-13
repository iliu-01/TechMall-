package com.techmall.order.mapper;

import com.techmall.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);

    Order selectById(@Param("id") Long id);

    List<Order> selectByUserId(@Param("userId") Long userId);

    List<Order> selectByMerchantId(@Param("merchantId") Long merchantId);

    List<Order> selectAll(@Param("userId") Long userId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
