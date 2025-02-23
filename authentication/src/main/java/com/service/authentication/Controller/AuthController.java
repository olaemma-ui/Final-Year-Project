package com.service.authentication.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.service.authentication.Auth.LoginService;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Enums.AccountType;
import com.service.authentication.ResetPassword.ResetPasswordModel;
import com.service.authentication.ResetPassword.ResetPasswordService;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Profile.ProfileService;
import com.service.authentication.Signup.SignUpModel;
import com.service.authentication.Signup.SignUpService;
import com.service.authentication.Verification.VerificationModel;
import com.service.authentication.Verification.VerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tgs-sgs-atm/api/")
public class AuthController {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    @Autowired
    private VerificationService verificationService;


    @PostMapping("auth/register")
    public ResponseEntity<ResponseModel> register(@Valid @RequestBody SignUpModel signUpModel, BindingResult bindingResult) {
        return signUpService.register(signUpModel, bindingResult);
    }

    @PostMapping("auth/login")
    public ResponseEntity<ResponseModel> login(@RequestBody JsonNode jsonNode) {
        return loginService.login(jsonNode);
    }

    @PostMapping("auth/forgetPassword")
    public ResponseEntity<ResponseModel> forgetPassword(@RequestParam(required = false) String email) {
        return resetPasswordService.forgotPassword(email);
    }

    @PostMapping("auth/resetPassword")
    public ResponseEntity<ResponseModel> resetPassword(@Valid @RequestBody ResetPasswordModel resetPasswordModel, BindingResult bindingResult) {
        return resetPasswordService.resetPassword(resetPasswordModel, bindingResult);
    }

    @PostMapping("verification/verify")
    public ResponseEntity<ResponseModel> verify(@RequestHeader("Authorization") String token, @Valid @RequestBody VerificationModel verificationModel, BindingResult bindingResult) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "OTP expired",
                    null,
                    null
            ));
        }
        return verificationService.verifyVerificationCode(token, verificationModel, bindingResult);
    }

//    @GetMapping("verification/send-otp")
//    public ResponseEntity<ResponseModel> sendVerification(@RequestParam String email) {
//        return verificationService.sendVerificationCode(email);
//    }

    @GetMapping("lecturers/all")
    public ResponseEntity<ResponseModel> getAllLecturers() {
        return signUpService.getAllUserByAccountType(AccountType.LECTURER.name());
    }


    @GetMapping("students/all")
    public ResponseEntity<ResponseModel> getAllStudents() {
        return signUpService.getAllUserByAccountType(AccountType.STUDENT.name());
    }

    @GetMapping("user/profile")
    public ResponseEntity<ResponseModel> getUserData(@RequestHeader("Authorization") String token) {
        String extractedToken = extractToken(token);

        // Validate token
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }

        return profileService.getUserData(extractedToken);
    }

    // Utility method to extract Bearer token
    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
