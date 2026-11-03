package org.example.javaframe.auth;

import org.example.javaframe.common.BizException;
import org.example.javaframe.common.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {

    private record Entry(String username, Instant expiresAt) {
    }

    private final ConcurrentHashMap<String, Entry> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public RefreshTokenStore(@Value("${app.jwt.refresh-token-days}") long days) {
        this(days, Clock.systemUTC());
    }

    RefreshTokenStore(long days, Clock clock) {
        this.ttl = Duration.ofDays(days);
        this.clock = clock;
    }

    public String issue(String username) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(sha256(token), new Entry(username, Instant.now(clock).plus(ttl)));
        return token;
    }

    public String rotate(String token) {
        Entry entry = tokens.remove(sha256(token));
        if (entry == null || Instant.now(clock).isAfter(entry.expiresAt())) {
            throw new BizException(ErrorCode.REFRESH_INVALID);
        }
        return entry.username();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("无法计算刷新票指纹", ex);
        }
    }
}
