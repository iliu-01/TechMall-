package com.techmall.user.controller;

import com.techmall.common.dto.PageDTO;
import com.techmall.common.result.Result;
import com.techmall.user.dto.LoginDTO;
import com.techmall.user.dto.RegisterDTO;
import com.techmall.user.entity.User;
import com.techmall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        Map<String, Object> result = userService.register(registerDTO);
        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<User> me(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getMe(userId);
        return Result.success(user);
    }

    @PutMapping("/me")
    public Result<User> updateMe(@RequestHeader("X-User-Id") Long userId, @RequestBody User user) {
        User updated = userService.updateMe(userId, user);
        return Result.success(updated);
    }

    @PutMapping("/recharge")
    public Result<?> recharge(@RequestHeader("X-User-Id") Long userId,
                              @RequestBody Map<String, Object> body) {
        return userService.recharge(userId, new java.math.BigDecimal(body.get("amount").toString()));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable("id") Long id) {
        User user = userService.getUser(id);
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<PageDTO<User>> list(@RequestParam(name = "role", required = false) String role,
                                       @RequestParam(name = "keyword", required = false) String keyword,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        PageDTO<User> pageDTO = userService.listUsers(role, keyword, page, size);
        return Result.success(pageDTO);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, Integer> body) {
        userService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/merchants")
    public Result<List<User>> getMerchants() {
        List<User> merchants = userService.getMerchants();
        return Result.success(merchants);
    }
}
