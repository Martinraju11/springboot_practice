package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.TokenRefreshException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ManagerService managerService;

    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setAge(registerRequest.getAge());
        user.setGender(registerRequest.getGender());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : Role.ROLE_EMPLOYEE);
        user.setDepartment(registerRequest.getDepartment());

        if (registerRequest.getManagerId() != null) {
            User manager = userRepository.findById(registerRequest.getManagerId()).orElse(null);
            user.setManager(manager);
            if (manager != null) {
                // Invalidate manager's cached team list
                managerService.evictTeamCache(manager.getId());
            }
        }

        user.setIsActive(true);
        return userRepository.save(user);
    }

    public AuthResult authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 1. Generate short-lived Access Token cookie (15 mins)
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // 2. Generate long-lived Refresh Token in DB & cookie (7 days)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie refreshTokenCookie = jwtUtils.generateRefreshTokenCookie(refreshToken.getToken());

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        UserInfoResponse userInfo = buildUserInfoResponse(user);

        return new AuthResult(userInfo, jwtCookie, refreshTokenCookie);
    }

    public TokenRefreshResult refreshToken(HttpServletRequest request) {
        String requestRefreshToken = jwtUtils.getRefreshTokenFromCookies(request);

        if (requestRefreshToken == null || requestRefreshToken.trim().isEmpty()) {
            throw new TokenRefreshException("", "Refresh Token is missing from cookies!");
        }

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Generate new Access Token
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
                    ResponseCookie newJwtCookie = ResponseCookie.from("jwt", newAccessToken)
                            .path("/")
                            .maxAge(900) // 15 mins
                            .httpOnly(true)
                            .sameSite("Lax")
                            .build();

                    // Refresh Token Rotation: generate a fresh refresh token
                    RefreshToken rotatedToken = refreshTokenService.createRefreshToken(user.getId());
                    ResponseCookie newRefreshCookie = jwtUtils.generateRefreshTokenCookie(rotatedToken.getToken());

                    return new TokenRefreshResult(newJwtCookie, newRefreshCookie, "Token refreshed successfully!");
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found in database!"));
    }

    public List<ResponseCookie> logoutUser(HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshTokenFromCookies(request);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteByToken(refreshToken);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }

        SecurityContextHolder.clearContext();

        ResponseCookie cleanJwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie cleanRefreshCookie = jwtUtils.getCleanRefreshTokenCookie();

        return List.of(cleanJwtCookie, cleanRefreshCookie);
    }

    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);

        return buildUserInfoResponse(user);
    }

    public UserInfoResponse buildUserInfoResponse(User user) {
        if (user == null) return null;
        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getGender(),
                user.getRole(),
                user.getDepartment(),
                user.getManager() != null ? user.getManager().getId() : null,
                user.getManager() != null ? user.getManager().getName() : null
        );
    }
}
