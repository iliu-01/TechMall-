package com.techmall.user.controller;

import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.ResultCode;
import com.techmall.user.entity.User;
import com.techmall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userMapper.selectById(id);
    }

    @GetMapping("/{id}/role")
    public Map<String, String> getRole(@PathVariable("id") Long id) {
        User user = userMapper.selectById(id);
        Map<String, String> result = new HashMap<>();
        result.put("role", user != null ? user.getRole() : null);
        return result;
    }

    /** 扣减余额，返回剩余余额 */
    @PutMapping("/{id}/deduct-balance")
    public Map<String, Object> deductBalance(@PathVariable("id") Long id,
                                              @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(400, "余额不足");
        }
        BigDecimal newBalance = user.getBalance().subtract(amount);
        userMapper.updateBalance(id, newBalance);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", newBalance);
        return result;
    }
}
