package com.service.authentication.ResetPassword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Mail.MailBody;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ResetPasswordService {
    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailController mailController;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VerificationService verificationService;

    public ResponseEntity<ResponseModel> resetPassword(ResetPasswordModel forgotPasswordModel, BindingResult bindingResult) {
        reset();

        data = forgotPasswordModel;
        try {
            if (bindingResult.hasErrors()) {
                collectFieldErrors(bindingResult);
                responseMessage = "Fill the required fields and provide valid data";
                httpStatus = HttpStatus.BAD_REQUEST;
            } else if (!forgotPasswordModel.getPassword().equals(forgotPasswordModel.getConfirmPassword())) {
                responseMessage = "Password mismatch!";
                httpStatus = HttpStatus.BAD_REQUEST;
            } else {
                handlePasswordReset(forgotPasswordModel);
            }
        } catch (Exception e) {
            handleException(e);
        }

        return buildResponse();
    }


    public ResponseEntity<ResponseModel> forgotPassword(String email) {
        reset();

        try {
            Optional<UserModel> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty()) {
                responseMessage = "User not found.";
                httpStatus = HttpStatus.NOT_FOUND;
            } else {
                UserModel userModel = optionalUser.get();
                Map<String, Object> claims = new HashMap<>();
                claims.put("email", userModel.getEmail());
                claims.put("verificationStatus", "pending");
                claims.put("userId", userModel.getUserId());
                claims.put("resetRequestType", "forgotPassword");

                Date now = new Date();
                Date expirationDate = new Date(now.getTime() + 5 * 60 * 1000); // Token expiration in 1 hour

                String token = JwtEncryptionDecryption.generateToken(claims, userModel.getEmail(), expirationDate);

                sendVerificationMail(userModel, token);
                responseMessage = "Verification code sent successfully.";
                success(token);  // Return the token in the response
            }
        } catch (Exception e) {
            handleException(e);
        }

        return buildResponse();
    }



    private void handlePasswordReset(ResetPasswordModel forgotPasswordModel) {
        try {
            String token = forgotPasswordModel.getToken();
            Map<String, Object> claims = JwtEncryptionDecryption.extractAllClaims(token);
            String email = (String) claims.get("email");
            String storedVerificationStatus = (String) claims.get("verificationStatus");
            Date expiredDate = JwtEncryptionDecryption.getTokenExpiration(token);

            if ("verified".equals(storedVerificationStatus) && !expiredDate.before(new Date())) {
                Optional<UserModel> optionalUser = userRepository.findByEmail(email);

                if (optionalUser.isPresent()) {
                    UserModel userModel = optionalUser.get();
                    updatePassword(userModel, forgotPasswordModel);
                } else {
                    responseMessage = "User not found.";
                    httpStatus = HttpStatus.NOT_FOUND;
                }
            } else {
                responseMessage = "Invalid or expired token.";
                httpStatus = HttpStatus.UNAUTHORIZED;
            }
        } catch (Exception e) {
            responseMessage = "An error occurred while validating the token.";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            error.put("exception", e.getMessage());
        }
    }

    private void updatePassword(UserModel userModel, ResetPasswordModel forgotPasswordModel) {
        String newPasswordHash = DigestUtils.sha256Hex(forgotPasswordModel.getConfirmPassword());

        if (forgotPasswordModel.getPassword().equals(newPasswordHash)) {
            responseMessage = "New password can't be the same as the old password.";
            httpStatus = HttpStatus.BAD_REQUEST;
        } else {
            userModel.setPassword(newPasswordHash);
            userRepository.save(userModel);

            mailController.send(new MailBody(
                    userModel.getEmail(),
                    "Dear " + userModel.getFirstName() + " " + userModel.getLastName() + ", your password has been reset successfully",
                    "tgs-sgs-atm Foods",
                    "Password Reset Successful"
            ));

            responseMessage = "Password reset successful.";
            success(forgotPasswordModel);
        }
    }

    private void sendVerificationMail(UserModel userModel, String token) {
        mailController.send(new MailBody(
                userModel.getEmail(),
                "Dear " + userModel.getFirstName() + " " + userModel.getLastName() + ", here is your password reset token: " + token,
                "tgs-sgs-atm Foods",
                "Password Reset Token"
        ));
    }

    private void collectFieldErrors(BindingResult bindingResult) {
        error = new HashMap<>();
        bindingResult.getFieldErrors().forEach(e ->
                error.put(e.getField(), e.getDefaultMessage())
        );
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        responseMessage = "Something went wrong";
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        responseCode = "500";
        error.put("exception", e.getMessage());
    }

    private ResponseEntity<ResponseModel> buildResponse() {
        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    private void success(Object data) {
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        this.httpStatus = HttpStatus.OK;
        error = null;
    }

    private void reset() {
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.BAD_REQUEST;
    }
}
