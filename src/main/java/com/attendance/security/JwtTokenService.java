package com.attendance.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    public static final long EXPIRY_SECONDS = 30 * 60;
    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public Token create(String username, String role) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(EXPIRY_SECONDS, ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("attendance-management")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(username)
                .claim("roles", java.util.List.of(role))
                .build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Token(value, expiresAt);
    }

    public record Token(String value, Instant expiresAt) { }
}
