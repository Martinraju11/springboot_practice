package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AuthService;
import com.example.demo.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // 1. IP-Based Rate Limiting Check (Token Bucket Algorithm)
        String clientIp = rateLimitService.getClientIp(request);
        if (!rateLimitService.tryConsume(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Rate limit exceeded: Too many login attempts. Please wait 1 minute before trying again."));
        }

        // 2. Delegate authentication, access token & refresh token generation to AuthService
        AuthResult authResult = authService.authenticateUser(loginRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authResult.getJwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, authResult.getRefreshTokenCookie().toString())
                .body(authResult.getUserInfo());
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        TokenRefreshResult refreshResult = authService.refreshToken(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshResult.getJwtCookie().toString())
                .header(HttpHeaders.SET_COOKIE, refreshResult.getRefreshTokenCookie().toString())
                .body(new MessageResponse(refreshResult.getMessage()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        List<ResponseCookie> cleanCookies = authService.logoutUser(request);

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        for (ResponseCookie cookie : cleanCookies) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return responseBuilder.body(new MessageResponse("You've been signed out!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserInfoResponse userInfo = authService.getCurrentUser();

        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated"));
        }

        return ResponseEntity.ok(userInfo);
    }
}
