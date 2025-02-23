package com.service.authentication.Auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LoginService {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;

    private static final String SECRET_KEY = "your_secret_key"; // Replace with your secret key for JWT signing

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VerificationService verificationService;

    // Main login method
    public ResponseEntity<ResponseModel> login(JsonNode jsonNode) {
        reset();

        try {
            AtomicBoolean valid = new AtomicBoolean(true);

            // Validate input
            if ((jsonNode.has("email") || jsonNode.has("phone")) && jsonNode.has("password")) {
                String password = DigestUtils.sha256Hex(jsonNode.get("password").asText());
                jsonNode.forEach(e -> valid.set(!(Optional.ofNullable(e).orElse(mapper.createObjectNode()).asText().isEmpty())));

                if (valid.get()) {
                    responseMessage = "Account does not exist";
                    // Check if the user exists by either phone or email
                    userRepository.findByPhoneOrEmail(
                                    Optional.ofNullable(jsonNode.get("phone")).orElse(mapper.createObjectNode()).asText(),
                                    Optional.ofNullable(jsonNode.get("email")).orElse(mapper.createObjectNode()).asText())
                            .ifPresent(userModel -> {
                                if (userModel.getPassword().equalsIgnoreCase(password)) {
                                    if (userModel.getStatus().equalsIgnoreCase(AccountStatus.PENDING.name())) {
                                        handlePendingAccount(userModel);
                                    } else {
                                        generateAndReturnToken(userModel);
                                    }
                                } else {
                                    responseMessage = "Invalid login credentials";
                                }
                            });
                } else {
                    responseMessage = "Enter required fields";
                }
            } else {
                responseMessage = "Fill in required fields";
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage = "Something went wrong";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseCode = "500";
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    // Handle unverified (pending) account
    private void handlePendingAccount(UserModel userModel) {
        responseMessage = "Account not verified: A verification code has been sent to your registered email.";
        httpStatus = HttpStatus.UNAUTHORIZED;
        responseCode = "99";
        try {
            data = verificationService.sendVerificationCode(userModel.getEmail(), VerificationType.PROFILE.name());
            responseMessage = "Your account is yet to be verified.";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Generate JWT token and return response with user data
    private void generateAndReturnToken(UserModel userModel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userModel.getUserId());
        claims.put("status", userModel.getStatus());
        claims.put("role", userModel.getAccountType());

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000); // Token expiration in 30 days

        try {
            String token = JwtEncryptionDecryption.generateToken(claims, userModel.getEmail(), expirationDate);
            success(token, userModel);  // Return both the JWT token and user data
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Format the successful response
    private void success(String token, UserModel userModel) {
        responseCode = "00";
        responseMessage = "Success";
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", userModel);
        this.data = responseData;  // Include both token and user data
        this.httpStatus = HttpStatus.OK;
        error = null;
    }

    // Reset response variables for each request
    private void reset() {
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.BAD_REQUEST;
    }
}
