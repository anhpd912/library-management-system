package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.AuthResponse;
import fa.training.librarymanagementsystem.dto.LoginRequest;
import fa.training.librarymanagementsystem.dto.RefreshTokenRequest;
import fa.training.librarymanagementsystem.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String username);
}