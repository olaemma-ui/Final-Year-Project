package com.service.authentication.Profile;


import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
public class ProfileService {


    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;

    private HttpStatus httpStatus;

    @Autowired
    private UserRepository userRepository;


    public ResponseEntity<ResponseModel> getUserData(String token){
        reset();

        try{

            Optional.ofNullable(token)
                    .ifPresent(value -> {

                        responseMessage = "Invalid or Expired token";
                        httpStatus = HttpStatus.NOT_FOUND;
                        responseCode = "99";
                        userRepository.findByToken(token).ifPresent(userModel -> {
                            if (userModel.getAccountStatus().equalsIgnoreCase(AccountStatus.ACTIVE.name())) {
                                success(userModel);
                            } else {
                                responseMessage = "Account not Verified";
                                httpStatus = HttpStatus.FORBIDDEN;
                                responseCode = "99";
                            }
                        });
                    });

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
        data = null;
    }
}
