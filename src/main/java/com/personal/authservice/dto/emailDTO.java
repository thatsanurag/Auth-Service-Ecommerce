package com.personal.authservice.dto;

import lombok.Data;

@Data
public class emailDTO {
    private String to;
    private String from;
    private String subject;
    private String body;
}
