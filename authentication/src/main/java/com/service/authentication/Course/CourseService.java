package com.service.authentication.Course;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.authentication.Encryption.JwtEncryptionDecryption;
import com.service.authentication.Message.ResponseModel;
import com.service.authentication.Model.UserModel;
import com.service.authentication.Repository.CourseRepository;
import com.service.authentication.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Service
@Validated
public class CourseService {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
    private HttpStatus httpStatus;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private LecturerCourseRepository lecturerCourseRepository;

    @Autowired
    private ObjectMapper mapper;

    public CourseService(CourseRepository courseRepository, ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper = mapper;
        this.courseRepository = courseRepository;
    }

    public ResponseEntity<ResponseModel> createCourse(String token, CourseModel courseModel, BindingResult bindingResult) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            if (bindingResult.hasErrors()) {
                handleValidationErrors(bindingResult);
                return buildResponse();
            }

            if (courseRepository.findByCourseCode(courseModel.getCourseCode()).isPresent()) {
                responseMessage = "Course code already exists.";
                httpStatus = HttpStatus.CONFLICT;
                return buildResponse();
            }

            courseModel.setId(UUID.randomUUID().toString());
            courseRepository.save(courseModel);
            successResponse(courseModel);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }

    public ResponseEntity<ResponseModel> updateCourse(String token, String id, CourseModel courseModel, BindingResult bindingResult) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            if (bindingResult.hasErrors()) {
                handleValidationErrors(bindingResult);
                return buildResponse();
            }

            Optional<CourseModel> existingCourse = courseRepository.findById(id);
            if (existingCourse.isEmpty()) {
                responseMessage = "Course not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            CourseModel updatedCourse = existingCourse.get();
            updatedCourse.setCourseTitle(courseModel.getCourseTitle());
            updatedCourse.setCourseCode(courseModel.getCourseCode());
            updatedCourse.setCourseUnit(courseModel.getCourseUnit());

            courseRepository.save(updatedCourse);
            successResponse(updatedCourse);

        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }

        return buildResponse();
    }

    public ResponseEntity<ResponseModel> getAllCourses() {
        resetState();
        try {
            data = courseRepository.findAll();
            httpStatus = HttpStatus.OK;
            successResponse(data);
        } catch (Exception e) {
            handleUnexpectedError();
        }
        return buildResponse();
    }

    public ResponseEntity<ResponseModel> getCourseByCode(String courseCode) {
        resetState();
        try {
            Optional<CourseModel> course = courseRepository.findByCourseCode(courseCode);
            if (course.isPresent()) {
                successResponse(course.get());
            } else {
                responseMessage = "Course not found.";
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } catch (Exception e) {
            handleUnexpectedError();
        }
        return buildResponse();
    }

    public ResponseEntity<ResponseModel> deleteCourse(String token, String id) {
        resetState();
        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            Optional<CourseModel> existingCourse = courseRepository.findById(id);
            if (existingCourse.isEmpty()) {
                responseMessage = "Course not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            courseRepository.deleteById(id);
            responseMessage = "Course deleted successfully.";
            httpStatus = HttpStatus.OK;
            successResponse(null);
        } catch (Exception e) {
            e.printStackTrace();
            handleUnexpectedError();
        }
        return buildResponse();
    }


    public ResponseEntity<ResponseModel> unassignCourseFromLecturer(String token, String lecturerId, String courseId) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            // Validate lecturer exists
            Optional<UserModel> lecturer = userRepository.findById(lecturerId);
            if (lecturer.isEmpty() || !"LECTURER".equals(lecturer.get().getAccountType())) {
                responseMessage = "User is not a Lecturer or does not exist.";
                httpStatus = HttpStatus.BAD_REQUEST;
                return buildResponse();
            }

            // Validate course exists
            Optional<CourseModel> course = courseRepository.findById(courseId);
            if (course.isEmpty()) {
                responseMessage = "Course not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            // Check if the lecturer is assigned to the course
            if (!courseRepository.existsByLecturerIdAndCourseId(lecturerId, courseId)) {
                responseMessage = "Lecturer is not assigned to this course.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            // Remove course from lecturer's list
            UserModel updatedLecturer = lecturer.get();
            List<CourseModel> updatedCourses = updatedLecturer.getCourses();
            updatedCourses.removeIf(c -> c.getId().equals(courseId));
            updatedLecturer.setCourses(updatedCourses);

            // Remove lecturer from course's list
            CourseModel updatedCourse = course.get();
            updatedCourse.setLecturers(null);

            // Save changes
            userRepository.save(updatedLecturer);
            courseRepository.save(updatedCourse);

            successResponse("Course unassigned successfully.");
        } catch (Exception e) {
            handleUnexpectedError();
        }

        return buildResponse();
    }



    public ResponseEntity<ResponseModel> assignCourseToLecturer(String token, String lecturerId, String courseId) {
        resetState();

        try {
            if (!isAuthorized(token)) {
                return unauthorizedResponse();
            }

            // Validate lecturer exists
            Optional<UserModel> lecturer = userRepository.findById(lecturerId);
            if (lecturer.isEmpty() || !"LECTURER".equals(lecturer.get().getAccountType())) {
                responseMessage = "User is not a Lecturer or does not exist.";
                httpStatus = HttpStatus.BAD_REQUEST;
                return buildResponse();
            }

            // Validate course exists
            Optional<CourseModel> course = courseRepository.findById(courseId);
            if (course.isEmpty()) {
                responseMessage = "Course not found.";
                httpStatus = HttpStatus.NOT_FOUND;
                return buildResponse();
            }

            // Check if already assigned
            if (courseRepository.existsByLecturerIdAndCourseId(lecturerId, courseId)) {
                responseMessage = "Lecturer is already assigned to this course.";
                httpStatus = HttpStatus.CONFLICT;
                return buildResponse();
            }

            // Assign course to lecturer (Many-to-Many)
            UserModel updatedLecturer  = lecturer.get();
            List<CourseModel> updatedCourseList = lecturer.get().getCourses();
            updatedCourseList.add(course.get());

            updatedLecturer.setCourses(updatedCourseList);
            course.get().setLecturers(updatedLecturer);

            userRepository.save(updatedLecturer);
            courseRepository.save(course.get());

            successResponse("Course assigned successfully.");
        } catch (Exception e) {
            handleUnexpectedError();
        }

        return buildResponse();
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

    private void handleValidationErrors(BindingResult bindingResult) {
        error = new HashMap<>();
        bindingResult.getFieldErrors().forEach(e -> error.put(e.getField(), e.getDefaultMessage()));
        responseMessage = "Fill the required fields with valid data";
        httpStatus = HttpStatus.BAD_REQUEST;
    }

    private void successResponse(Object data) {
        responseCode = "00";
        responseMessage = "Success";
        this.data = data;
        httpStatus = HttpStatus.CREATED;
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

    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
