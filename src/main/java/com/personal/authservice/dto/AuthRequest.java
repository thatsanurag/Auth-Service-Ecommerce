package com.personal.authservice.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
