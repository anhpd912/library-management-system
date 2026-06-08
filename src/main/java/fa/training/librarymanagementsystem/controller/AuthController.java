package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.ApiResponse;
import fa.training.librarymanagementsystem.dto.AuthResponse;
import fa.training.librarymanagementsystem.dto.LoginRequest;
import fa.training.librarymanagementsystem.dto.RefreshTokenRequest;
import fa.training.librarymanagementsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Handles authentication endpoints: login, token refresh, and logout. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refresh(request)));
    }

    /** Requires a valid access token — username is extracted from the JWT via SecurityContext. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
