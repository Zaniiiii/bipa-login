package com.example.auth_service.service;

import com.example.auth_service.entity.User;
import com.example.auth_service.entity.VerificationToken;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(false); // Account is disabled until email is verified
        User savedUser = userRepository.save(user);

        // Generate verification token
        String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser, token);

        // Send verification email
        emailService.sendVerificationEmail(savedUser, token);

        return savedUser;
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24)) // Token valid for 24 hours
                .build();
        tokenRepository.save(verificationToken);
    }

    public String validateVerificationToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return "invalidToken";
        }
        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.isExpired()) {
            return "expired";
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return "valid";
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}