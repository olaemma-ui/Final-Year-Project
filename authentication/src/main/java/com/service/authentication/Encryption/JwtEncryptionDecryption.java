package com.service.authentication.Encryption;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class JwtEncryptionDecryption {

    private static final String SECRET_KEY = "01234567890123456789012345678901"; // Must be 32 bytes for HS256
    private static final SecretKey jwtKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Generate JWT Token
    public static String generateToken(Map<String, Object> claims, String subject, Date expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expiration) // Expiry Date
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }


    // Extract the user role from the token
    public static String extractUserRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null; // Return null if the role is not found or an error occurs
        }
    }

    // Check if user has a specific role
    public static boolean hasRole(String token, String role) {
        String userRole = extractUserRole(token);
        return userRole != null && userRole.equalsIgnoreCase(role);
    }



    // Extract all claims
    public static Claims extractAllClaims(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // Extract a specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Check if the token is expired
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Extract expiration date from the token
    public static Date getTokenExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // Extract the username (subject) from the token
    public static String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate token
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
