package com.service.admin.Encryption;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

public class JwtEncryptionDecryption {

    private static final String SECRET_KEY = "your-secret-key"; // Replace with your actual secret

    // Generate JWT Token
    public static String generateToken(Map<String, Object> claims, SecretKey encryptionKey) throws Exception {
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject("Lecturer")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiry
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();

        // Encrypt JWT
        return AESUtil.encrypt(jwt, encryptionKey);
    }

    // Decrypt and Parse JWT Token
    public static Map<String, Object> decryptToken(String encryptedToken, SecretKey encryptionKey) throws Exception {
        // Decrypt the JWT
        String jwt = AESUtil.decrypt(encryptedToken, encryptionKey);

        // Parse the JWT
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

}
