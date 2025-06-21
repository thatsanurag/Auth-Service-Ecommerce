package com.personal.authservice.exception;

public class LoginDetailsNotFoundException extends RuntimeException {
    public LoginDetailsNotFoundException(String message) {
        super(message);
    }
}
