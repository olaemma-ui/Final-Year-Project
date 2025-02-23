package com.service.authentication.Signup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.authentication.Enums.AccountStatus;
import com.service.authentication.Enums.AccountType;
import com.service.authentication.Enums.VerificationType;
import com.service.authentication.Mail.MailController;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.UserRepository;
import com.service.authentication.Verification.VerificationModel;
import com.service.authentication.Verification.VerificationService;
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
    private VerificationService verificationService;

    @Autowired
    private MailController mailController;

    public SignUpService(UserRepository userRepository, MailController mailController, ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.mailController = mailController;
    }

    public ResponseEntity<ResponseModel> register(SignUpModel signUpModel, BindingResult bindingResult) {
        resetState();

        try {
            if (bindingResult.hasErrors()) {
                handleValidationErrors(bindingResult);
                return buildResponse();
            }

            if (!signUpModel.getPassword().equalsIgnoreCase(signUpModel.getConfirmPassword())) {
                responseMessage = "Password mismatch";
                return buildResponse();
            }

            if (isUserOrEmailExists(signUpModel)) {
                return buildResponse();
            }

            UserModel userModel = createUser(signUpModel);
            ResponseEntity<ResponseModel> verificationResponse = verificationService.sendVerificationCode(userModel.getEmail(), VerificationType.PROFILE.name());

            if ((verificationResponse.getBody() != null) && (verificationResponse.getBody().getData() instanceof VerificationModel)) {
                VerificationModel verification = mapper.convertValue(verificationResponse.getBody().getData(), VerificationModel.class);
                userModel.setToken(verification.getToken());
            }

            userRepository.save(userModel);
            httpStatus = HttpStatus.CREATED;
            successResponse(signUpModel);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }


    public ResponseEntity<ResponseModel> getAllUserByAccountType(String accountType) {
        resetState();

        try {
            httpStatus = HttpStatus.OK;
            successResponse(userRepository.findByAccountType(accountType));
        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }

    private void handleValidationErrors(BindingResult bindingResult) {
        error = new HashMap<>();
        bindingResult.getFieldErrors().forEach(e -> error.put(e.getField(), e.getDefaultMessage()));
        responseMessage = "Fill the required fields with valid data";
        httpStatus = HttpStatus.BAD_REQUEST;
    }

    private boolean isUserOrEmailExists(SignUpModel signUpModel) {
        boolean emailExists = userRepository.findByEmail(signUpModel.getEmail()).isPresent();
        boolean userExists = signUpModel.getAccountType().equalsIgnoreCase(AccountType.STUDENT.name())
                && userRepository.findById(signUpModel.getIdentifier()).isPresent();

        if (emailExists || userExists) {
            responseMessage = emailExists
                    ? "Email already exists, do you want to login?"
                    : "This student " + signUpModel.getIdentifier() + " already exists, do you want to login?";
            httpStatus = HttpStatus.CONFLICT;
            return true;
        }
        return false;
    }

    private UserModel createUser(SignUpModel signUpModel) {
        UserModel userModel = mapper.convertValue(signUpModel, UserModel.class);
        userModel.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userModel.setStatus(AccountStatus.PENDING.name());
        userModel.setUserId(UUID.randomUUID().toString());
        userModel.setPassword(DigestUtils.sha256Hex(signUpModel.getPassword()));
        return userModel;
    }

    private void successResponse(Object data) {
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        error = null;
    }

    private void handleUnexpectedError() {
        responseCode = "500";
        responseMessage = "Something went wrong";
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private void resetState() {
        responseCode = "96";
        responseMessage = null;
        error = null;
        httpStatus = HttpStatus.BAD_REQUEST;
        data = null;
    }

    private ResponseEntity<ResponseModel> buildResponse() {
        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }
}
