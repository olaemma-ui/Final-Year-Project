package com.service.authentication.ForgotPassword;


import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationModel;
import com.service.authentication.Verification.VerificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

@Service
public class ForgotPasswordService {
    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;

    private HttpStatus httpStatus;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private MailController mailController;

    @Autowired
    private ObjectMapper mapper;


    public ResponseEntity<ResponseModel> verify(ForgotPasswordModel forgotPasswordModel, BindingResult bindingResult){
        reset();

        data = forgotPasswordModel;
        try{

            if (bindingResult.hasErrors()){
                error = new HashMap<>();

                bindingResult.getFieldErrors().forEach(e->
                        error.put(e.getField(), e.getDefaultMessage())
                );
                responseMessage = "Fill the required fields and valid data";
                httpStatus = HttpStatus.BAD_REQUEST;
            }else {
                if (forgotPasswordModel.getPassword().equalsIgnoreCase(forgotPasswordModel.getConfirmPassword())) {
                    responseMessage = "Invalid token";
                    verificationRepository.findById(forgotPasswordModel.getToken()).ifPresent(
                            verificationModel -> {
                                userRepository.findById(verificationModel.getUserId()).ifPresent(
                                        userModel -> {
                                            String psw = DigestUtils.sha256Hex(forgotPasswordModel.getConfirmPassword());
                                            if (!userModel.getPassword().equalsIgnoreCase(psw)) {
                                                userModel.setPassword(psw);

                                                verificationRepository.delete(verificationModel);
                                                userRepository.save(userModel);
                                                success(forgotPasswordModel);
//                                                mailController.send(new MailBody(
//                                                        userModel.getEmail(),
//                                                        "Dear "+userModel.getFirstName()+" "+ userModel.getLastName()+", your password has been reset successfully",
//                                                        "6AM Foods",
//                                                        "Password Reset Successful"
//                                                ));
                                            }else responseMessage = "New password can't be same as old password";
                                        }
                                );
                            }
                    );
                }else responseMessage = "Password mismatch!";
            }
        }catch (Exception e){
            e.printStackTrace();
            responseMessage = "Something went wrong";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseCode = "500";
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    public ResponseEntity<ResponseModel> sendMail(String email, String phone){
        reset();

        try{
            if (email != null || phone != null) {
                responseMessage = "Account does not exist";
                userRepository.findByPhoneOrEmail(phone, email).ifPresent(
                        userModel -> {
                            String code = String.valueOf(new Random().nextInt(11111) + 88888);
                            String sha256hex = DigestUtils.sha256Hex(code);
                            System.out.println("code = " + code);


                            VerificationModel verificationModel = new VerificationModel();
                            verificationModel.setCodeHash(sha256hex);
                            verificationModel.setUserId(userModel.getId());
                            verificationModel.setVerificationType(VerificationType.FORGET_PASSWORD.name());
                            verificationModel.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime());
                            verificationModel.setToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));
                            verificationModel.setResendToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));
                            verificationRepository.save(verificationModel);
//                            mailController.send(new MailBody(
//                                    userModel.getEmail(),
//                                    "Dear User complete verification with the code below " + code,
//                                    "olaemma4213@gmail.com",
//                                    "6AM Verification"));

                            success(new HashMap<String, String>() {{
                                put("resendToken", verificationModel.getResendToken());
                                put("token", verificationModel.getToken());
                            }});
                        }
                );
            }else responseMessage = "Select either email or phone for verification";

        }catch (Exception e){
            e.printStackTrace();
            responseMessage = "Something went wrong";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseCode = "500";
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    private void success(Object data){
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        this.httpStatus = HttpStatus.OK;
        error = null;
    }

    private void reset(){
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.BAD_REQUEST;
    }

}
