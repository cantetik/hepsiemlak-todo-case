package com.hepsiemlak.todo.util;

import com.hepsiemlak.todo.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String username, Long lastModifiedDate) {
        return Jwts.builder()
                .claim("lastModifiedDate", lastModifiedDate)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .compact();
    }

    public boolean validateToken(String authorization, String username, Long lastModifiedDate) {
        String token = extractToken(authorization);
        return (extractUsernameByToken(token).equals(username) &&
                !extractClaim(token, Claims::getExpiration).before(new Date()) &&
                extractLastModification(token) >= lastModifiedDate);
    }

    public String extractUsernameByAuthorization(String authorization) {
        return extractClaim(extractToken(authorization), Claims::getSubject);
    }

    public String extractUsernameByToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Long extractLastModification(String token) {
        return extractClaim(token, claims -> claims.get("lastModifiedDate", Long.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("No token provided");
        }

        return authorization.substring(7);
    }
}