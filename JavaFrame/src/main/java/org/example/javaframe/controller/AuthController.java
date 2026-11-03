package org.example.javaframe.controller;

import jakarta.validation.Valid;
import org.example.javaframe.auth.DemoUser;
import org.example.javaframe.auth.JwtService;
import org.example.javaframe.auth.LoginRequest;
import org.example.javaframe.auth.RefreshRequest;
import org.example.javaframe.auth.RefreshTokenStore;
import org.example.javaframe.common.BizException;
import org.example.javaframe.common.ErrorCode;
import org.example.javaframe.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final DemoUser demoUser;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthController(DemoUser demoUser, JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.demoUser = demoUser;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        if (!demoUser.matches(request.username(), request.password())) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return tokensFor(request.username());
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request) {
        String username = refreshTokenStore.rotate(request.refreshToken());
        return tokensFor(username);
    }

    private Result<Map<String, String>> tokensFor(String username) {
        return Result.success(Map.of(
                "token", jwtService.issue(username),
                "refreshToken", refreshTokenStore.issue(username)
        ));
    }
}