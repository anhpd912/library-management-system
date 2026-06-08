package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.AuthResponse;
import fa.training.librarymanagementsystem.dto.LoginRequest;
import fa.training.librarymanagementsystem.dto.RefreshTokenRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String username);
}
