package com.service.authentication.Encryption;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
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

//    public static void main(String[] args) throws Exception {
//        // Generate AES Key for encryption
//        SecretKey encryptionKey = AESUtil.generateKey();
//
//        // Create claims for the JWT
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("name", "Dr. John Doe");
//        claims.put("email", "johndoe@school.edu");
//        claims.put("role", "Lecturer");
//
//        // Generate Encrypted JWT
//        String encryptedToken = generateToken(claims, encryptionKey);
//        System.out.println("Encrypted JWT: " + encryptedToken);
//
//        // Decrypt JWT and read claims
//        Map<String, Object> decryptedClaims = decryptToken(encryptedToken, encryptionKey);
//        System.out.println("Decrypted Claims: " + decryptedClaims);
//    }
}
