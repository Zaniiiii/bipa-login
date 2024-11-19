package com.example.auth_service.controller;

import com.example.auth_service.entity.LoginHistory;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.PasswordResetTokenRepository;
import com.example.auth_service.service.EmailService;
import com.example.auth_service.service.UserService;
import com.example.auth_service.security.JwtTokenProvider;
import com.example.auth_service.repository.LoginHistoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${LOGIN_URL}")
    private String loginUrl;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, @RequestParam(required = false) String role) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email sudah digunakan");
        }
        User registeredUser = userService.registerUser(user, role);
        return ResponseEntity.ok("Pendaftaran Berhasil. Silahkan periksa pos-el anda untuk verifikasi.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            String redirectUrl = loginUrl;
            String htmlResponse = "<html>" +
                    "<body>" +
                    "<p>Email telah berhasil diverifikasi. Anda akan diteruskan ke halaman masuk...</p>" +
                    "<script>" +
                    "setTimeout(function() {" +
                    "window.location.href = '" + redirectUrl + "';" +
                    "}, 3000);" +  // Redirect setelah 5 detik (5000 ms)
                    "</script>" +
                    "</body>" +
                    "</html>";
            return ResponseEntity.ok(htmlResponse);
        } else if (result.equals("expired")) {
            return ResponseEntity.badRequest().body("Token verifikasi sudah kadaluarsa.");
        } else {
            return ResponseEntity.badRequest().body("Token verifikasi tidak valid.");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Pengguna tidak ditemukan"));

        if (!user.isEnabled()) {
            return ResponseEntity.badRequest().body("Email belum terverifikasi. Periksa email anda.");
        }

        if (!jwtTokenProvider.getPasswordEncoder().matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Kredensial tidak valid");
        }

        // **Simpan kejadian login ke LoginHistory**
        LoginHistory loginHistory = LoginHistory.builder()
                .userId(user.getId())
                .build();
        loginHistoryRepository.save(loginHistory);

        String token = jwtTokenProvider.createToken(email, user.getRole(), user.getUsername(), String.valueOf(user.getId()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    // API khusus untuk admin
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return userService.getAllUsers(PageRequest.of(page, size, sort));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Pengguna berhasil dihapus");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/logged-in-last-24-hours")
    public ResponseEntity<Long> countUsersLoggedInLast24Hours() {
        long count = userService.countUsersLoggedInLast24Hours();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/registration-count")
    public ResponseEntity<Long> getRegistrationCount(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        long count = userService.countRegisteredUsers(year, month);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/recent-logins")
    public ResponseEntity<List<Map<String, Object>>> getRecentLogins(
            @RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> recentLogins = userService.getRecentLogins(limit);
        return ResponseEntity.ok(recentLogins);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/active-details")
    public ResponseEntity<List<Map<String, Object>>> getActiveUsers() {
        List<Map<String, Object>> activeUsers = userService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/count-by-country")
    public ResponseEntity<Page<Map<String, Object>>> getUserCountByCountry(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "country") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        // Daftar field yang diizinkan untuk sorting
        List<String> allowedSortFields = Arrays.asList("country", "count");

        if (!allowedSortFields.contains(sortBy)) {
            return ResponseEntity.badRequest().body(null);
        }

        // Karena 'count' bukan properti entitas langsung, kita perlu membuat Sort.Order secara manual
        Sort.Order sortOrder = new Sort.Order(
                direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );

        Sort sort = Sort.by(sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Map<String, Object>> result = userService.getUserCountByCountry(country, pageable);

        return ResponseEntity.ok(result);
    }

    // Endpoint untuk meminta reset password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Email tidak ditemukan");
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetToken(user, token);
        emailService.sendPasswordResetEmail(user, token);

        return ResponseEntity.ok("Email untuk reset password telah dikirim");
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        String result = userService.validatePasswordResetToken(token);
        if (result.equals("valid")) {
            return ResponseEntity.ok("Token valid.");
        } else if (result.equals("expired")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token reset password telah kadaluarsa.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token reset password tidak valid.");
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        String result = userService.resetPassword(token, newPassword);

        if (result.equals("invalidToken") || result.equals("expired")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token tidak valid atau telah kadaluarsa.");
        } else if (result.equals("userNotFound")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pengguna tidak ditemukan.");
        } else if (result.equals("success")) {
            return ResponseEntity.ok("Password berhasil direset.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Terjadi kesalahan.");
        }
    }

}