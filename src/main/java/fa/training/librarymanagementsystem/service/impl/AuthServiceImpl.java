package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.config.JwtUtil;
import fa.training.librarymanagementsystem.dto.response.AuthResponse;
import fa.training.librarymanagementsystem.dto.request.LoginRequest;
import fa.training.librarymanagementsystem.dto.request.RefreshTokenRequest;
import fa.training.librarymanagementsystem.dto.request.RegisterRequest;
import fa.training.librarymanagementsystem.entity.RefreshToken;
import fa.training.librarymanagementsystem.entity.User;
import fa.training.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.AuthService;
import fa.training.librarymanagementsystem.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Handles credential verification, JWT issuance, token refresh, and logout. */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.READER)
                .active(true)
                .build();

        userRepository.save(user);
    }

    /**
     * Authenticates credentials and returns an access token + refresh token pair.
     * AuthenticationManager throws BadCredentialsException on failure,
     * caught by GlobalExceptionHandler and returned as 401.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    /**
     * Issues a new access token and rotates the refresh token.
     * Rotation invalidates the old refresh token immediately, limiting replay attack windows.
     */
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = oldRefreshToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtUtil.generateToken(userDetails);
        // Rotate: old token is deleted inside createRefreshToken before saving the new one
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    /** Revokes the refresh token so the user cannot obtain new access tokens without logging in again. */
    @Override
    public void logout(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> refreshTokenService.deleteByUserId(user.getId()));
    }
}