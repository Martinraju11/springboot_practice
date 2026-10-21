package com.example.demo.dto;

import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class TokenRefreshResult {
    private ResponseCookie jwtCookie;
    private ResponseCookie refreshTokenCookie;
    private String message;

    public TokenRefreshResult() {
    }

    public TokenRefreshResult(ResponseCookie jwtCookie, ResponseCookie refreshTokenCookie, String message) {
        this.jwtCookie = jwtCookie;
        this.refreshTokenCookie = refreshTokenCookie;
        this.message = message;
    }
}

