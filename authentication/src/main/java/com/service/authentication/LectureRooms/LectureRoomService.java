package com.service.authentication.LectureRooms;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Repository.LectureRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
public class LectureRoomService {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;

    @Autowired
    private LectureRoomRepository lectureRoomRepository;

    @Autowired
    private ObjectMapper mapper;

    public LectureRoomService(LectureRoomRepository lectureRoomRepository, ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.lectureRoomRepository = lectureRoomRepository;
    }

    public ResponseEntity<ResponseModel> createLectureRoom(String token, LectureRoom lectureRoom, BindingResult bindingResult) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            if (bindingResult.hasErrors()) {
                handleValidationErrors(bindingResult);
                return buildResponse();
            }

            lectureRoom.setId(UUID.randomUUID().toString());
            lectureRoomRepository.save(lectureRoom);
            httpStatus = HttpStatus.CREATED;
            successResponse(lectureRoom);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }

    public ResponseEntity<ResponseModel> updateLectureRoom(String token, String id, LectureRoom lectureRoom, BindingResult bindingResult) {
        resetState();

        try {

            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            if (bindingResult.hasErrors()) {
                handleValidationErrors(bindingResult);
                return buildResponse();
            }

            Optional<LectureRoom> existingRoom = lectureRoomRepository.findById(id);
            if (existingRoom.isEmpty()) {
                responseMessage = "Lecture room not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            LectureRoom updatedRoom = existingRoom.get();
            updatedRoom.setRoomName(lectureRoom.getRoomName());
            updatedRoom.setLocation(lectureRoom.getLocation());
            updatedRoom.setCapacity(lectureRoom.getCapacity());
            updatedRoom.setWidth(lectureRoom.getWidth());
            updatedRoom.setLength(lectureRoom.getLength());
            updatedRoom.setLatitude(lectureRoom.getLatitude());
            updatedRoom.setLongitude(lectureRoom.getLongitude());

            lectureRoomRepository.save(updatedRoom);
            httpStatus = HttpStatus.CREATED;
            successResponse(updatedRoom);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }

    public ResponseEntity<ResponseModel> getLectureRoomById(String id) {
        resetState();
        try {
            Optional<LectureRoom> room = lectureRoomRepository.findById(id);
            if (room.isPresent()) {
                httpStatus = HttpStatus.OK;
                successResponse(room.get());
            } else {
                responseMessage = "Lecture room not found.";
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } catch (Exception e) {
            handleUnexpectedError();
        }
        return buildResponse();
    }

    public ResponseEntity<ResponseModel> deleteLectureRoom(String token, String id) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            Optional<LectureRoom> existingRoom = lectureRoomRepository.findById(id);
            if (existingRoom.isEmpty()) {
                responseMessage = "Lecture room not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            lectureRoomRepository.deleteById(id);
            httpStatus = HttpStatus.OK;
            responseMessage = "Lecture room deleted successfully.";
            successResponse(null);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }


    public ResponseEntity<ResponseModel> getAllLectureRooms() {
        resetState();
        try {
            data = lectureRoomRepository.findAll();
            httpStatus = HttpStatus.OK;
            successResponse(data);
        } catch (Exception e) {
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


    private boolean isAuthorized(String token) {
        String extractedToken = extractToken(token);
        return extractedToken != null && !JwtEncryptionDecryption.isTokenExpired(extractedToken) &&
                JwtEncryptionDecryption.hasRole(extractedToken, "ADMIN");
    }

    private ResponseEntity<ResponseModel> unauthorizedResponse() {
        responseCode = "403";
        responseMessage = "Unauthorized action";
        httpStatus = HttpStatus.FORBIDDEN;
        return buildResponse();
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


    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
    private ResponseEntity<ResponseModel> buildResponse() {
        return new ResponseEntity<>(new ResponseModel(responseCode, responseMessage, data, error), httpStatus);
    }
}
