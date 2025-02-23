package com.service.authentication.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.service.authentication.Course.CourseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;


@Entity(name = "User")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserModel {


    @Id
    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String matricNumber;

    private String status;

    private String accountType;

    @Transient
    private String token;

    @JsonIgnore
    private String password;

    private String photoUrl;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @OneToMany(mappedBy = "lecturers")
    private List<CourseModel> courses;
}
