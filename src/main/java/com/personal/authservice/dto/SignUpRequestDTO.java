package com.personal.authservice.dto;

import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String email;
    private String password;

}
