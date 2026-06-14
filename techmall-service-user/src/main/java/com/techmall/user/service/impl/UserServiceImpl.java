package com.techmall.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.techmall.common.dto.PageDTO;
import com.techmall.common.exception.BusinessException;
import com.techmall.common.result.ResultCode;
import com.techmall.common.utils.JwtUtils;
import com.techmall.user.dto.LoginDTO;
import com.techmall.user.dto.RegisterDTO;
import com.techmall.user.entity.User;
import com.techmall.user.mapper.UserMapper;
import com.techmall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.techmall.common.result.Result;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> register(RegisterDTO registerDTO) {
        // 检查用户名唯一
        User exist = userMapper.selectByUsername(registerDTO.getUsername());
        if (exist != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 角色只能 USER 或 MERCHANT
        String role = registerDTO.getRole();
        if (role == null || (!role.equals("USER") && !role.equals("MERCHANT"))) {
            role = "USER";
        }

        // BCrypt 加密密码
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(encodedPassword);
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setRole(role);
        user.setStatus(1);

        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // BCrypt 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 检查状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 签发 JWT
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 返回 token + 用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("nickname", user.getNickname());
        result.put("balance", user.getBalance());
        return result;
    }

    @Override
    public User getMe(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 清空密码
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateMe(Long userId, User user) {
        User exist = userMapper.selectById(userId);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setId(userId);
        // 不允许修改 role 和 status
        user.setRole(null);
        user.setStatus(null);
        user.setPassword(null);
        userMapper.updateById(user);
        return getMe(userId);
    }

    @Override
    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public PageDTO<User> listUsers(String role, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> list = userMapper.selectList(role, keyword);
        PageInfo<User> pageInfo = new PageInfo<>(list);
        // 清空所有用户的密码
        for (User user : list) {
            user.setPassword(null);
        }
        return PageDTO.of(list, pageInfo.getTotal(), page, size);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 不能禁用 ADMIN
        if ("ADMIN".equals(user.getRole()) && (status == null || status == 0)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        userMapper.updateStatus(id, status);
    }

    @Override
    public List<User> getMerchants() {
        List<User> merchants = userMapper.selectList("MERCHANT", null);
        for (User user : merchants) {
            user.setPassword(null);
        }
        return merchants;
    }

    @Override
    public Result<?> recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail(400, "充值金额必须大于0");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        BigDecimal newBalance = user.getBalance().add(amount);
        userMapper.updateBalance(userId, newBalance);
        user.setBalance(newBalance);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", newBalance);
        return Result.success(data);
    }
}
