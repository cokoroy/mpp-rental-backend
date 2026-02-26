package com.mpp.rental.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil - Utility class for JWT token operations
 * Handles token generation, validation, and extraction of claims
 * Updated for JJWT 0.12.x API
 */
@Component
public class JwtUtil {

    // Secret key for signing JWT tokens (should be stored in application.properties)
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String SECRET_KEY;

    // Token expiration time in milliseconds (24 hours)
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // Token expiration for "Remember Me" (7 days)
    @Value("${jwt.expiration.remember:604800000}")
    private long jwtExpirationRememberMe;

    /**
     * Extract username (email) from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * UPDATED: Using parser() instead of parserBuilder() for JJWT 0.12.x
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())  // Changed from setSigningKey
                .build()
                .parseSignedClaims(token)     // Changed from parseClaimsJws
                .getPayload();                // Changed from getBody
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate JWT token for user
     * @param userDetails User details
     * @param rememberMe If true, token expires in 7 days; otherwise 24 hours
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), rememberMe);
    }

    /**
     * Generate JWT token with additional claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, boolean rememberMe) {
        return createToken(extraClaims, userDetails.getUsername(), rememberMe);
    }

    /**
     * Create JWT token
     * UPDATED: Using builder() and signWith() for JJWT 0.12.x
     */
    private String createToken(Map<String, Object> claims, String subject, boolean rememberMe) {
        long expirationTime = rememberMe ? jwtExpirationRememberMe : jwtExpiration;

        return Jwts
                .builder()
                .claims(claims)                                              // Changed from setClaims
                .subject(subject)                                            // Changed from setSubject
                .issuedAt(new Date(System.currentTimeMillis()))             // Changed from setIssuedAt
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // Changed from setExpiration
                .signWith(getSignInKey())                                    // Simplified signWith
                .compact();
    }

    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Get signing key for JWT
     * UPDATED: Returns SecretKey instead of Key
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}