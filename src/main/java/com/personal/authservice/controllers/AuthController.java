package com.personal.authservice.controllers;

import com.personal.authservice.dto.*;
import com.personal.authservice.services.IAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        SignUpResponseDTO signUpResponseDTO  = new SignUpResponseDTO();
        if(authService.signup(signUpRequestDTO)) {
            signUpResponseDTO.setStatus(RequestStatus.SUCCESS);
            return ResponseEntity.status(HttpStatus.CREATED).body(signUpResponseDTO);
        }
        signUpResponseDTO.setStatus(RequestStatus.FAILURE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(signUpResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO  = new LoginResponseDTO();
//        if(loginRequestDTO.getEmail() == null || loginRequestDTO.getPassword() == null) {
//            loginResponseDTO.setStatus(RequestStatus.FAILURE);
//            loginResponseDTO.setToken("Something went wrong!!");
//            loginResponseDTO.setMessage("Email or password is empty");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResponseDTO);
//        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String token = authService.login(loginRequestDTO);
        headers.add("Authorization", token);
        loginResponseDTO.setToken(token);
        loginResponseDTO.setStatus(RequestStatus.SUCCESS);
        loginResponseDTO.setMessage("Login successful!");
        return new ResponseEntity<>(loginResponseDTO, headers, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("validateToken")
    public boolean validateToken(@RequestHeader("Authorization") String token) {
        return authService.authenticate(token);
    }
}
