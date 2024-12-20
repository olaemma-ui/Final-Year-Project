package com.service.admin.lecturers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.admin.Enums.AccountStatus;
import com.service.admin.Enums.AccountType;
import com.service.admin.Mail.MailBody;
import com.service.admin.Mail.MailController;
import com.service.admin.Message.ResponseModel;
import com.service.admin.Profile.UserModel;
import com.service.admin.Profile.UserRepository;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class LecturerService {


    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;

    private HttpStatus httpStatus;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    private MailController mailController;

    LecturerService(UserRepository userRepository, ObjectMapper mapper, MailController mailController) {
        this.userRepository = userRepository;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.mailController = mailController;
    }


    public ResponseEntity<ResponseModel> createLecturerProfile(UserModel userModel, BindingResult bindingResult) {
        reset();

        try {

            if (bindingResult.hasErrors()) {

                error = new HashMap<>();

                bindingResult.getFieldErrors().forEach(e ->
                        error.put(e.getField(), e.getDefaultMessage())
                );
                responseMessage = "Fill the required fields and valid data";
                httpStatus = HttpStatus.BAD_REQUEST;
            } else {
                boolean emailExist = userRepository.findByEmail(userModel.getEmail()).isPresent();
                boolean phoneExist = false;
                if (!emailExist) {
                    if (userModel.getPhone() != null) {
                        phoneExist = userRepository.findByPhone(userModel.getPhone()).isPresent();
                    }
                    if (!phoneExist) {

                        userModel.setAccountType(AccountType.LECTURER.name());

                        String mailBody = _generateLecturerWelcomeEmail(
                                userModel.getFirstName(),
                                userModel.getEmail(),
                                userModel.getPassword()
                        );

                        mailController.send(new MailBody(
                                userModel.getEmail(),
                                mailBody,
                                "olaemma4213@gmail.com",
                                "TGS Profile Created"
                        ));

                        userModel.setPassword(DigestUtils.sha256Hex(userModel.getPassword()));
                        userRepository.save(userModel);
                        success(userModel);
                    } else {
                        responseMessage = "Phone number already exist, do you want to login ?";
                    }
                } else {
                    responseMessage = "Email already exist !!!";
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
            responseMessage = "Something went wrong";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseCode = "500";
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }

    public ResponseEntity<ResponseModel> updateLecturerProfile(UserModel userModel, BindingResult bindingResult) {
        reset();

        try {

            if (bindingResult.hasErrors()) {

                error = new HashMap<>();

                bindingResult.getFieldErrors().forEach(e ->
                        error.put(e.getField(), e.getDefaultMessage())
                );
                responseMessage = "Fill the required fields and valid data";
                httpStatus = HttpStatus.BAD_REQUEST;
            } else {
                boolean emailExist = userRepository.findByEmail(userModel.getEmail()).isPresent();
                boolean phoneExist = false;
                if (emailExist) {
                    if (userModel.getPhone() != null) {
                        phoneExist = userRepository.findByPhone(userModel.getPhone()).isPresent();
                    }
                    if (!phoneExist) {
                        userModel.setAccountType(AccountType.LECTURER.name());
                        String mailBody = _generateLecturerWelcomeEmail(
                                userModel.getFirstName(),
                                userModel.getEmail(),
                                userModel.getPassword()
                        );

                        mailController.send(new MailBody(
                                userModel.getEmail(),
                                mailBody,
                                "olaemma4213@gmail.com",
                                "TGS Profile Update"
                        ));
                        userRepository.save(userModel);
                        success(userModel);
                    } else {
                        responseMessage = "Phone number already exist, do you want to login ?";
                    }
                } else {
                    responseMessage = "Email provided is Invalid";
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
            responseMessage = "Something went wrong";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            responseCode = "500";
        }

        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }


    private static String _generateLecturerWelcomeEmail(String lecturerName, String email, String password) {
        String primaryColor = "#007BFF"; // Blue color
        String systemName = "TGS-SG-ATM";

        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "    body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f9; color: #333; }" +
                        "    .container { max-width: 600px; margin: 20px auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }" +
                        "    .header { background-color: %s; color: #fff; text-align: center; padding: 15px; border-radius: 8px 8px 0 0; }" +
                        "    .content { padding: 20px; }" +
                        "    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #555; }" +
                        "    .details { font-weight: bold; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <div class='header'><h1>Welcome to %s</h1></div>" +
                        "        <div class='content'>" +
                        "            <p>Dear <span class='details'>%s</span>,</p>" +
                        "            <p>We are pleased to inform you that you have been successfully added to the school system. Below are your login details:</p>" +
                        "            <p>Email: <span class='details'>%s</span><br>Password: <span class='details'>%s</span></p>" +
                        "            <p>Please use these credentials to log in to the system. We recommend changing your password upon your first login for security reasons.</p>" +
                        "            <p>If you have any questions or need assistance, feel free to reach out to the IT department.</p>" +
                        "        </div>" +
                        "        <div class='footer'>Thank you for being part of our system! <br> TGS-SG-ATM Administration</div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>",
                primaryColor, systemName, lecturerName, email, password
        );
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
