package org.example.javaframe.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoUser {

    private final String username;
    private final String passwordHash;
    private final PasswordEncoder passwordEncoder;

    public DemoUser(PasswordEncoder passwordEncoder,
                    @Value("${app.auth.username}") String username,
                    @Value("${app.auth.password}") String password) {
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.passwordHash = passwordEncoder.encode(password);
    }

    public boolean matches(String username, String rawPassword) {
        return this.username.equals(username) && passwordEncoder.matches(rawPassword, passwordHash);
    }
}