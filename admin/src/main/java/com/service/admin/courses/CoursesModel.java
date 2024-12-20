package com.service.admin.courses;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity(name = "courses")
@Getter
@Setter
public class CoursesModel {

    @Id
    String courseId = UUID.randomUUID().toString();


    @Pattern(regexp = "^[a-zA-Z0-9\\s]{5,}$", message = "Enter a valid course title")
    String courseCode;

    @Pattern(regexp = "^[a-zA-Z0-9\\s]$", message = "Enter a valid course title")
    String courseTitle;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[0-9]{1}$", message = "Enter a valid course unit")
    String courseUnit;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "(ND 1|ND 2|HND1|HND 2)", message = "Select a course level")
    String courseLevel;
}
