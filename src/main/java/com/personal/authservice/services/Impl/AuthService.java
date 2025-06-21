package com.personal.authservice.services.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.authservice.clients.KafkaProducerClient;
import com.personal.authservice.dto.AuthResponse;
import com.personal.authservice.dto.LoginRequestDTO;
import com.personal.authservice.dto.SignUpRequestDTO;
import com.personal.authservice.dto.emailDTO;
import com.personal.authservice.exception.LoginDetailsNotFoundException;
import com.personal.authservice.exception.UserAlreadyExistsException;
import com.personal.authservice.exception.WrongPasswordException;
import com.personal.authservice.models.Role;
import com.personal.authservice.models.Session;
import com.personal.authservice.models.SessionStatus;
import com.personal.authservice.models.User;
import com.personal.authservice.repositories.UserSessionRepository;
import com.personal.authservice.repositories.UserRepository;
import com.personal.authservice.services.IAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@RequiredArgsConstructor
@Service
public class AuthService implements IAuthService {

    private UserRepository userRepository;
    private UserSessionRepository userSessionRepository;
    private KafkaProducerClient kafkaProducerClient;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private SecretKey secretKey = Jwts.SIG.HS256.key().build();

    private ObjectMapper objectMapper;

    @Autowired
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserSessionRepository userSessionRepository,
                       KafkaProducerClient kafkaProducerClient, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userSessionRepository = userSessionRepository;
        this.kafkaProducerClient = kafkaProducerClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        Optional<User> user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if(loginRequestDTO.getEmail() == null || loginRequestDTO.getPassword() == null) {
            throw new LoginDetailsNotFoundException("Email or password is empty!!");
        }
        if(user.isEmpty()) {
            throw new LoginDetailsNotFoundException("Email address does not exist. Please register yourself!!");
        }
        boolean matches = bCryptPasswordEncoder.matches(loginRequestDTO.getPassword(), user.get().getPassword());
        if(!matches) {
            throw new WrongPasswordException("Password doesn't match!");
        };
        String token = createJwtToken(user.get().getId(), user.get().getRoles(), user.get().getEmail());
        Session session = new Session();
        session.setToken(token);
        session.setUser(user.get());

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 24);
        Date endDate = calendar.getTime();

        session.setExpiringAt(endDate);
        session.setStatus(SessionStatus.ACTIVE);
        userSessionRepository.save(session);

        return token;
    }

    @Override
    public void logout() {

    }

    @Override
    public boolean signup(SignUpRequestDTO signUpRequestDTO) {
        if(signUpRequestDTO.getEmail() == null || signUpRequestDTO.getPassword() == null) {
            return false;
        }
        if(userRepository.existsByEmail(signUpRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("User with Email: " + signUpRequestDTO.getEmail() +  " already exists, please try sign in");
        }
        User user = new User();
        user.setEmail(signUpRequestDTO.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(signUpRequestDTO.getPassword()));
        userRepository.save(user);

        //Send message to Kafka for welcome email
        emailDTO emailDTO = new emailDTO();
        emailDTO.setTo(signUpRequestDTO.getEmail());
        emailDTO.setFrom("xoom.aj@gmail.com");
        emailDTO.setSubject("Welcome to our website");
        emailDTO.setBody("Have a pleasent experience here");

        try {
            kafkaProducerClient.send("user_signed", objectMapper.writeValueAsString(emailDTO));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        return true;
    }

    @Override
    public boolean authenticate(String token) {
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

        return true;
    }

    @Override
    public AuthResponse generateAuthResponse(User user) {
        return null;
    }

    @Override
    public void logoutAllDevices(String email) {

    }

    private String createJwtToken(Long userId, Set<Role> roles, String email) {
        Map<String, Object> dataInJwt = new HashMap<>();
        dataInJwt.put("user_id", userId);
        dataInJwt.put("roles", roles);
        dataInJwt.put("email", email);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 24);
        Date endDate = calendar.getTime();
        return Jwts.builder()
                .claims(dataInJwt)
                .expiration(endDate)
                .issuedAt(new Date())
                .signWith(secretKey)
                .compact();
    }
}
