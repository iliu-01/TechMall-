package com.techmall.user.controller;

import com.techmall.user.entity.User;
import com.techmall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userMapper.selectById(id);
    }

    @GetMapping("/{id}/role")
    public Map<String, String> getRole(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        Map<String, String> result = new HashMap<>();
        result.put("role", user != null ? user.getRole() : null);
        return result;
    }
}
