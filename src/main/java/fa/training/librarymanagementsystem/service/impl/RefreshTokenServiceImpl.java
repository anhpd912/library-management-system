package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.entity.RefreshToken;
import fa.training.librarymanagementsystem.entity.User;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.exception.TokenExpiredException;
import fa.training.librarymanagementsystem.repository.RefreshTokenRepository;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Manages the lifecycle of refresh tokens: creation, validation, and revocation. */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new refresh token for the given user.
     * Any existing token is deleted first — one active token per user.
     */
    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Revoke the old token before issuing a new one
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush(); // Ensure deletion is committed before saving new token

        return refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());
    }

    /**
     * Validates that the token exists and has not expired.
     * Expired tokens are deleted from the DB before throwing so they cannot be reused.
     */
    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired, please login again");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId)
                .ifPresent(refreshTokenRepository::deleteByUser);
    }
}