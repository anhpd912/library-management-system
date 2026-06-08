package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken validateRefreshToken(String token);
    void deleteByUserId(Long userId);
}
