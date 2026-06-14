package com.techmall.user.service;

import com.techmall.common.dto.PageDTO;
import com.techmall.user.dto.LoginDTO;
import com.techmall.user.dto.RegisterDTO;
import com.techmall.user.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public interface UserService {

    Map<String, Object> register(RegisterDTO registerDTO);

    Map<String, Object> login(LoginDTO loginDTO);

    User getMe(Long userId);

    User updateMe(Long userId, User user);

    User getUser(Long id);

    PageDTO<User> listUsers(String role, String keyword, int page, int size);

    void updateUserStatus(Long id, Integer status);

    java.util.List<User> getMerchants();

    com.techmall.common.result.Result<?> recharge(Long userId, BigDecimal amount);
}
