package com.vaultpay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private String message;
    private Long userId;
    private String username;
    private String role;
    private String token;
}