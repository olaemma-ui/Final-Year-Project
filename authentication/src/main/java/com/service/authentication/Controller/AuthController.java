package com.service.authentication.Controller;


import com.service.authentication.Auth.LoginService;
import com.service.authentication.ForgotPassword.ForgotPasswordModel;
import com.service.authentication.ForgotPassword.ForgotPasswordService;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Profile.ProfileService;
import com.service.authentication.Signup.SignUpModel;
import com.service.authentication.Signup.SignUpService;
import com.service.authentication.Verification.VerificationModel;
import com.service.authentication.Verification.VerificationService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("6am/api/")
public class AuthController {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private VerificationService verificationService;

    @PostMapping("auth/signup")
    private ResponseEntity<ResponseModel> signup(@Valid @RequestBody SignUpModel signUpModel, BindingResult bindingResult){
        return signUpService.signup(signUpModel, bindingResult);
    }

   @PostMapping("auth/login")
    private ResponseEntity<ResponseModel> login(@RequestBody JsonNode jsonNode){
        return loginService.login(jsonNode);
    }

   @PostMapping("auth/forgetPassword")
    private ResponseEntity<ResponseModel> forgetPassword(@RequestParam(required = false) String email, @RequestParam(required = false) String phone){
        return forgotPasswordService.sendMail(email, phone);
    }

   @PostMapping("auth/forgetPassword/verify")
    private ResponseEntity<ResponseModel> forgetPassword(@Valid @RequestBody ForgotPasswordModel forgotPasswordModel, BindingResult bindingResult){
        return forgotPasswordService.verify(forgotPasswordModel, bindingResult);
    }


    @PostMapping("verification/verify")
    private ResponseEntity<ResponseModel> verify(@Valid @RequestBody VerificationModel verificationModel, BindingResult bindingResult){
        return verificationService.verify(verificationModel, bindingResult);
    }

    @GetMapping("verification/resend")
    private ResponseEntity<ResponseModel> resendVerification(@RequestParam String resendToken){
        return verificationService.resendVerification(resendToken);
    }

    @GetMapping(value = "user/profile")
    private ResponseEntity<ResponseModel> getUserData(@RequestHeader("auth-token") String token){
        return profileService.getUserData(token);
    }
}
