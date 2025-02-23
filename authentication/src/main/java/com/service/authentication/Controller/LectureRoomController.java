package com.service.authentication.Controller;


import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.LectureRooms.LectureRoom;
import com.service.authentication.LectureRooms.LectureRoomService;
import com.service.authentication.Message.ResponseModel;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("tgs-sgs-atm/api/lecture-rooms")
public class LectureRoomController {

    private final LectureRoomService lectureRoomService;

    public LectureRoomController(LectureRoomService lectureRoomService) {
        this.lectureRoomService = lectureRoomService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseModel> createLectureRoom(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody LectureRoom lectureRoom,
            BindingResult bindingResult) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }
        return lectureRoomService.createLectureRoom(extractedToken, lectureRoom, bindingResult);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseModel> updateLectureRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @Valid @RequestBody LectureRoom lectureRoom,
            BindingResult bindingResult) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }
        return lectureRoomService.updateLectureRoom(extractedToken, id, lectureRoom, bindingResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel> getLectureRoomById(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }
        return lectureRoomService.getLectureRoomById(id);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseModel> getAllLectureRooms(@RequestHeader("Authorization") String token) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }
        return lectureRoomService.getAllLectureRooms();
    }


    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseModel> deleteRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }

        if (!JwtEncryptionDecryption.hasRole(extractedToken, "ADMIN")) {
            return ResponseEntity.status(403).body(new ResponseModel(
                    "403",
                    "Unauthorized action",
                    null,
                    null
            ));
        }

        return lectureRoomService.deleteLectureRoom(token, id);
    }


    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
