package com.service.authentication.Verification;

import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@Service
public class VerificationService {
    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailController mailController;

    public VerificationService(VerificationRepository verificationRepository, UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }


    public ResponseEntity<ResponseModel> verify(VerificationModel verificationModel, BindingResult bindingResult){
        reset();
        data = verificationModel;


        if (bindingResult.hasErrors()){
            error = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e->
                error.put(e.getField(), e.getDefaultMessage())
            );
            responseMessage = "All fields are required and valid";
        }else{
            responseMessage = "invalid verification token!!!";
            verificationRepository.findById(verificationModel.getToken()).ifPresent(
                    verification ->{

                        verificationModel.setResendToken(verification.getResendToken());
                        verificationModel.setToken(verification.getToken());

                        String sha256hex = DigestUtils.sha256Hex(verificationModel.getVerificationCode());
                        if(sha256hex.equalsIgnoreCase(verification.getCodeHash())){
                            if (verification.getVerificationType().equalsIgnoreCase(VerificationType.REGISTRATION.name())){
                                userRepository.findById(verification.getUserId()).ifPresent(
                                        userModel -> {
                                            verificationRepository.delete(verification);
                                            userModel.setAccountStatus(AccountStatus.ACTIVE.name());
                                            userModel.setResendToken(null);
                                            userRepository.save(userModel);
                                            success(Collections.EMPTY_MAP, HttpStatus.OK);
                                        }
                                );
                            }else{
                                verificationRepository.delete(verification);
                                success(verificationModel, HttpStatus.OK);
                            }
                        }else{
                            data = verificationModel;
                            responseMessage = "Invalid verification code";
                        }
                    }
            );
        }
        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    public ResponseEntity<ResponseModel> resendVerification(String resendToken){
        reset();

        responseMessage = "invalid resend token!!!";
        data = Collections.EMPTY_MAP;
        verificationRepository.findByResendToken(resendToken).ifPresent(
                verification ->{

                    String code = String.valueOf(new Random().nextInt(11111) + 88888);
                    String sha256hex = DigestUtils.sha256Hex(code);

                    System.out.println("code = "+code);
                    userRepository.findById(verification.getUserId()).ifPresent(
                            userModel -> {
                                verification.setCodeHash(sha256hex);
                                verificationRepository.save(verification);

//                                mailController.send(new MailBody(
//                                        userModel.getEmail(),
//                                        "Dear User complete verification with the code below " + code,
//                                        "olaemma4213@gmail.com",
//                                        "6AM Verification"));
                                success(verification, HttpStatus.OK);
                            }
                    );
                }
        );

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }


    private void success(Object data, HttpStatus httpStatus){
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        this.httpStatus = httpStatus;
        error = null;
    }

    private void reset(){
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
