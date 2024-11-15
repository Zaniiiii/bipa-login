package com.example.auth_service.service;

import com.example.auth_service.entity.LoginHistory;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.VerificationToken;
import com.example.auth_service.repository.LoginHistoryRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.repository.VerificationTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LoginHistoryRepository loginHistoryRepository;

    public User registerUser(User user, String role) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role != null ? role : "USER");
        user.setEnabled(false); // Account is disabled until email is verified
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        createVerificationToken(savedUser, token);
        emailService.sendVerificationEmail(savedUser, token);

        return savedUser;
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
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

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public User updateUser(UUID id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setCountry(updatedUser.getCountry());
        user.setRole(updatedUser.getRole());
        user.setEnabled(updatedUser.isEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public long countUsersLoggedInLast24Hours() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return loginHistoryRepository.countDistinctByUserIdAndLoginAtAfter(since);
    }

    public long countRegisteredUsers(Integer year, Integer month) {
        return userRepository.countByRegistrationDate(year, month);
    }

    public List<Map<String, Object>> getRecentLogins(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> recentLogins = loginHistoryRepository.findRecentUniqueLogins(pageable);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] record : recentLogins) {
            UUID userId = (UUID) record[0];
            LocalDateTime loginAt = (LocalDateTime) record[1];

            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getId());
                userData.put("username", user.getUsername());
                userData.put("loginAt", loginAt);
                result.add(userData);
            }
        }

        return result;
    }

    public List<Map<String, Object>> getActiveUsers() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<UUID> activeUserIds = loginHistoryRepository.findActiveUserIds(since);

        List<User> users = userRepository.findAllById(activeUserIds);

        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getId());
            userData.put("username", user.getUsername());
            userData.put("country", user.getCountry());
            return userData;
        }).collect(Collectors.toList());

        return result;
    }

}