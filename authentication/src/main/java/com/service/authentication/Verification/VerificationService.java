package com.service.authentication.Verification;

import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Mail.MailBody;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.*;

@Service
public class VerificationService {
    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailController mailController;

    public VerificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public ResponseEntity<ResponseModel> sendVerificationCode(String email, String verificationType) {
        reset();

        responseMessage = "Email does not exist!!!";
        httpStatus = HttpStatus.NOT_FOUND;
        data = Collections.emptyMap();

        Map<String, Object> claims = new HashMap<>();


        userRepository.findByEmail(email).ifPresent(userModel -> {

            try {

                String code = String.valueOf(new Random().nextInt(11111) + 88888);
                String sha256hex = DigestUtils.sha256Hex(code);

                claims.put("email", email);
                claims.put("code", sha256hex);
                claims.put("verificationType", verificationType);
                claims.put("userId", userModel.getUserId());
                claims.put("firstName", userModel.getFirstName());
//
                String token = JwtEncryptionDecryption.generateToken(
                        claims,
                        userModel.getEmail(),
                        new Date(System.currentTimeMillis() + 1000 * 60 * 5)  // 3 minutes
                );

                VerificationModel verification = new VerificationModel(email, null, token);

                System.out.println("code = " + code);

                mailController.send(
                        new MailBody(userModel.getEmail(),
                                "Dear User complete verification with the code below " +
                                        code + "<br> This otp will expire in exactly 5 minutes",
                                "olaemma4213@gmail.com",
                                "TGS-SGS-ATM Verification"
                        )
                );
                success(verification, HttpStatus.OK);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }


    public ResponseEntity<ResponseModel> verifyVerificationCode(String token, VerificationModel verificationModel, BindingResult bindingResult) {
        reset();

        if (bindingResult.hasErrors()) {
            error = new HashMap<>();

            bindingResult.getFieldErrors().forEach(e ->
                    error.put(e.getField(), e.getDefaultMessage())
            );
            responseMessage = "Fill the required fields with valid data";
            httpStatus = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
        }

        String email = verificationModel.getEmail();
        String code = verificationModel.getVerificationCode();

        // Fetch verification data based on the email
        Optional<UserModel> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            responseMessage = "Email does not exist";
            httpStatus = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
        }

        try {
            // Decode and verify the token
            Map<String, Object> claims = JwtEncryptionDecryption.extractAllClaims(token);
            String storedHash = (String) claims.get("code");
            String storedEmail = (String) claims.get("email");
            String verificationType = (String) claims.get("verificationType");

            System.out.println("claims = " + claims);

            if (!storedEmail.equals(email)) {
                responseMessage = "Unauthorized.";
                httpStatus = HttpStatus.UNAUTHORIZED;
                return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
            }

            // Hash the provided code and compare
            String providedHash = DigestUtils.sha256Hex(code);
            if (!storedHash.equals(providedHash)) {
                responseMessage = "Invalid verification code.";
                httpStatus = HttpStatus.UNAUTHORIZED;
                return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
            }

            // Check if the token has expired
            Date expirationDate = JwtEncryptionDecryption.getTokenExpiration(token);
            if (new Date().after(expirationDate)) {
                httpStatus = HttpStatus.UNAUTHORIZED;
                responseMessage = "Verification code has expired.";
                return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
            }

            // Verification successful - generate a new token with email, userId, and verificationStatus
            UserModel userModel = optionalUser.get();
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("email", email);
            newClaims.put("userId", userModel.getUserId());
            newClaims.put("verificationStatus", "verified"); // Set the status as verified

            // Generate the new encrypted token
            String newToken = JwtEncryptionDecryption.generateToken(
                    newClaims,
                    userModel.getEmail(),
                    new Date(System.currentTimeMillis() + 1000 * 60 * 5) // 5 minutes expiry
            );

            // Return the new token as part of the response
            Map<String, String> mapData = new HashMap<>();
            mapData.put("message", "Verification successful");
            mapData.put("token", newToken); // Include the new token here

            if (verificationType.equalsIgnoreCase(VerificationType.PROFILE.name())) {
                userModel.setStatus(AccountStatus.VERIFIED.name());
                userRepository.save(userModel);
            }if (verificationType.equalsIgnoreCase(VerificationType.FORGET_PASSWORD.name())) {

            }
            success(mapData, HttpStatus.OK);

        } catch (Exception e) {
            responseMessage = "An error occurred during verification.";
            error = new HashMap<>();
            error.put("exception", e.getMessage());
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }


    private void success(Object data, HttpStatus httpStatus) {
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        this.httpStatus = httpStatus;
        error = null;
    }

    private void reset() {
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
