package com.techmall.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private java.math.BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
