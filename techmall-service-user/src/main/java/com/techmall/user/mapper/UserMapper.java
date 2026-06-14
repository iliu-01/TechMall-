package com.techmall.user.mapper;

import com.techmall.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    List<User> selectList(@Param("role") String role, @Param("keyword") String keyword);

    int insert(User user);

    int updateById(User user);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateBalance(@Param("id") Long id, @Param("balance") java.math.BigDecimal balance);
}
