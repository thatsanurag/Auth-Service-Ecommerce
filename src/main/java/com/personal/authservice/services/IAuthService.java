package com.personal.authservice.services;

import com.personal.authservice.dto.*;
import com.personal.authservice.models.User;

public interface IAuthService {
    public String login(LoginRequestDTO loginRequestDTO);
    public void logout();
    public boolean signup(SignUpRequestDTO signUpRequestDTO);
    public boolean authenticate(String token);
    public AuthResponse generateAuthResponse(User user);
    public void logoutAllDevices(String email);
}
