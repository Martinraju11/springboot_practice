package com.example.demo.dto;

import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class AuthResult {
    private UserInfoResponse userInfo;
    private ResponseCookie jwtCookie;
    private ResponseCookie refreshTokenCookie;

    public AuthResult() {
    }

    public AuthResult(UserInfoResponse userInfo, ResponseCookie jwtCookie, ResponseCookie refreshTokenCookie) {
        this.userInfo = userInfo;
        this.jwtCookie = jwtCookie;
        this.refreshTokenCookie = refreshTokenCookie;
    }
}
