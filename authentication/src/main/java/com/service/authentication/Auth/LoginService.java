package com.service.authentication.Auth;


import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LoginService {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;

    private HttpStatus httpStatus;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VerificationService verificationService;

    public ResponseEntity<ResponseModel> login(JsonNode jsonNode){
        reset();

        try{
            AtomicBoolean valid = new AtomicBoolean(true);

            if ((jsonNode.has("email") || jsonNode.has("phone")) && jsonNode.has("password")){
                String psw = DigestUtils.sha256Hex(jsonNode.get("password").asText());
                jsonNode.forEach(
                        e-> valid.set(!(Optional.ofNullable(e).orElse(mapper.createObjectNode()).asText().isEmpty()))
                );
                if (valid.get()){
                    responseMessage = "Account does not exist";
                    userRepository.findByPhoneOrEmail(
                            Optional.ofNullable(jsonNode.get("phone")).orElse(mapper.createObjectNode()).asText(),
                            Optional.ofNullable(jsonNode.get("email")).orElse(mapper.createObjectNode()).asText()).ifPresent(
                            userModel -> {

                                if (userModel.getPassword().equalsIgnoreCase(psw)){
                                    if (userModel.getAccountStatus().equalsIgnoreCase(AccountStatus.PENDING.name())){
                                        responseMessage = "Account not verified : A Verification code has been sent to your registered email";
                                        httpStatus = HttpStatus.UNAUTHORIZED;
                                        data =  Objects.requireNonNull(verificationService.resendVerification(userModel.getResendToken()).getBody()).getData();
                                    }else {
                                        userModel.setToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));
                                        userRepository.save(userModel);
                                        success(userModel);
                                    }
                                }else responseMessage = "Invalid Login credentials";
                            }
                    );
                }else responseMessage = "Enter required fields";
            }else responseMessage = "Fill in required fields";

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
