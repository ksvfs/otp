package ru.otp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import ru.otp.config.AppProperties;
import ru.otp.dto.auth.AuthResponse;
import ru.otp.model.Role;
import ru.otp.model.User;

@Service
public class JwtService {

    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public AuthResponse generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(appProperties.getJwt().getTtlSeconds());
        String token = Jwts.builder()
                .subject(user.username())
                .claim("userId", user.id())
                .claim("role", user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();

        return new AuthResponse(token, "Bearer", expiresAt, user.username(), user.role().name());
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number userId = claims.get("userId", Number.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        return new AuthenticatedUser(userId.longValue(), username, Role.valueOf(role));
    }

    private SecretKey getSigningKey() {
        String secret = appProperties.getJwt().getSecret();
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException exception) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
