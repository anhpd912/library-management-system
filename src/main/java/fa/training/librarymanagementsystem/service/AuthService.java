package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.response.AuthResponse;
import fa.training.librarymanagementsystem.dto.request.LoginRequest;
import fa.training.librarymanagementsystem.dto.request.RefreshTokenRequest;
import fa.training.librarymanagementsystem.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String username);
}