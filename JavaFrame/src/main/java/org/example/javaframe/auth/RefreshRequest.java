package org.example.javaframe.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "刷新票不能为空") String refreshToken
) {
}