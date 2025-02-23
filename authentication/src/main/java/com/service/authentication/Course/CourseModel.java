package com.service.authentication.Course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.service.authentication.Model.UserModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class CourseModel {

    @Id
    private String id;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Enter a valid course title")
    private String courseTitle;


    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z1-9 ]+$", message = "Enter a valid course code")
    private String courseCode;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Enter the course description")
    private String courseDescription;

    @NotNull( message = "This field is required")
    private Integer courseUnit;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "(HND 1|ND 1|HND 2|ND 2)", message = "Select a valid level ")
    private String level;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;


    @ManyToOne
    @JoinTable(
            name = "course_lecturer",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "lecturer_id")

    )
    @JsonIgnore
    private UserModel lecturers;

}
