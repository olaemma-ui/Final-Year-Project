package com.service.authentication.Signup;


import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Mail.MailBody;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationModel;
import com.service.authentication.Verification.VerificationRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

@Service
@Validated
public class SignUpService {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;

    private HttpStatus httpStatus;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationRepository verificationRepository;


    @Autowired
    private MailController mailController;

    public SignUpService(UserRepository userRepository, MailController mailController, ObjectMapper mapper, VerificationRepository verificationRepository) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.mailController = mailController;
    }


    public ResponseEntity<ResponseModel> signup(SignUpModel signUpModel, BindingResult bindingResult){
        reset();

        data = signUpModel;
        try{

            if (bindingResult.hasErrors()){
                error = new HashMap<>();

                bindingResult.getFieldErrors().forEach(e->
                        error.put(e.getField(), e.getDefaultMessage())
                );
                responseMessage = "Fill the required fields and valid data";
                httpStatus = HttpStatus.BAD_REQUEST;
            }else {
                if (signUpModel.getPassword().equalsIgnoreCase(signUpModel.getConfirmPassword())){
                    boolean emailExist= userRepository.findByEmail(signUpModel.getEmail()).isPresent();
                    boolean phoneExist = false;
                    if (!emailExist) {
                        if (signUpModel.getPhone() != null){
                            phoneExist =userRepository.findByPhone(signUpModel.getPhone()).isPresent();
                        }
                        if (!phoneExist) {
                            String code = String.valueOf(new Random().nextInt(11111) + 88888);
                            String sha256hex = DigestUtils.sha256Hex(code);


                            System.out.println("code = "+code);
                            UserModel userModel = mapper.convertValue(signUpModel, UserModel.class);


                            VerificationModel verificationModel = new VerificationModel();
                            verificationModel.setCodeHash(sha256hex);
                            verificationModel.setUserId(userModel.getId());
                            verificationModel.setVerificationType(VerificationType.REGISTRATION.name());
                            verificationModel.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime());
                            verificationModel.setToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));
                            verificationModel.setResendToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));

                            signUpModel.setToken(verificationModel.getToken());
                            signUpModel.setResendToken(verificationModel.getResendToken());

                            userModel.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                            userModel.setAccountStatus(AccountStatus.PENDING.name());
                            userModel.setPassword(DigestUtils.sha256Hex(signUpModel.getPassword()));

                            verificationRepository.save(verificationModel);
                            userModel.setResendToken(verificationModel.getResendToken());
                            userModel.setToken(verificationModel.getToken());
                            userRepository.save(userModel);

                            mailController.send(new MailBody(
                                    userModel.getEmail(),
                                    "Dear User complete verification with the code below " + code,
                                    "olaemma4213@gmail.com",
                                    "6AM Verification"));

                            success(signUpModel);
                        }else{
                            responseMessage = "Phone number already exist, do you want to login ?";
                        }
                    }else{
                        responseMessage = "Email already exist, do you want to login ?";
                    }
                }else{
                    responseMessage = "Password mismatch";
                }

            }
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
        this.httpStatus = HttpStatus.CREATED;
        error = null;
    }

    private void reset(){
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.BAD_REQUEST;
    }
}
