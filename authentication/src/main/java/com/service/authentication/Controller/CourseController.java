package com.service.authentication.Controller;

import com.service.authentication.Course.CourseModel;
import com.service.authentication.Course.CourseService;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Message.ResponseModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tgs-sgs-atm/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<ResponseModel> createCourse(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CourseModel courseModel,
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


        if (!JwtEncryptionDecryption.hasRole(extractedToken, "ADMIN")) {
            return ResponseEntity.status(403).body(new ResponseModel(
                    "403",
                    "Unauthorized action",
                    null,
                    null
            ));
        }

        return courseService.createCourse(token, courseModel, bindingResult);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseModel> updateCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @Valid @RequestBody CourseModel courseModel,
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

        if (!JwtEncryptionDecryption.hasRole(extractedToken, "ADMIN")) {
            return ResponseEntity.status(403).body(new ResponseModel(
                    "403",
                    "Unauthorized action",
                    null,
                    null
            ));
        }

        return courseService.updateCourse(token, id, courseModel, bindingResult);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseModel> getAllCourses(@RequestHeader("Authorization") String token) {
        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }
        return courseService.getAllCourses();
    }

    @GetMapping("/{courseCode}")
    public ResponseEntity<ResponseModel> getCourseByCode(
            @RequestHeader("Authorization") String token,
            @PathVariable String courseCode) {

        String extractedToken = extractToken(token);
        if (JwtEncryptionDecryption.isTokenExpired(extractedToken)) {
            return ResponseEntity.status(401).body(new ResponseModel(
                    "401",
                    "Token expired",
                    null,
                    null
            ));
        }

        return courseService.getCourseByCode(courseCode);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseModel> deleteCourse(
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

        return courseService.deleteCourse(token, id);
    }

    @PostMapping("/assign")
    public ResponseEntity<ResponseModel> assignCourse(
            @RequestHeader("Authorization") String token,
            @RequestParam("lecturerId") String lecturerId,
            @RequestParam("courseId") String courseId) {

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
        return courseService.assignCourseToLecturer(token, lecturerId, courseId);
    }



    @PostMapping("/unassign")
    public ResponseEntity<ResponseModel> unassignCourseFromLecturer(
            @RequestHeader("Authorization") String token,
            @RequestParam("lecturerId") String lecturerId,
            @RequestParam("courseId") String courseId) {

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
        return courseService.unassignCourseFromLecturer(token, lecturerId, courseId);
    }


    // Utility method to extract Bearer token
    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}