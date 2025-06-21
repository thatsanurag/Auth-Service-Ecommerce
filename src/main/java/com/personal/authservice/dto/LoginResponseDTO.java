package com.personal.authservice.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private RequestStatus status;
    private String token;
    private String message;
}
