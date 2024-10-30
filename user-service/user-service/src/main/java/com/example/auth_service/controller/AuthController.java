package com.example.auth_service.controller;

import com.example.auth_service.entity.User;
import com.example.auth_service.service.UserService;
import com.example.auth_service.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
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

}