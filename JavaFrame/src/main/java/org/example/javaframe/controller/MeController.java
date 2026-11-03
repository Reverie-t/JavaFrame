package org.example.javaframe.controller;

import org.example.javaframe.common.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    @GetMapping("/me")
    public Result<Map<String, String>> me(Authentication authentication) {
        return Result.success(Map.of("username", authentication.getName()));
    }
}