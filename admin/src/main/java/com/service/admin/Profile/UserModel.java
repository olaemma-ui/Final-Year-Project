package com.service.admin.Profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;



@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserModel {


    @Id
    private String id = UUID.randomUUID().toString();


    @Pattern(regexp = "^[a-zA-Z0-9\\s]{5,}$", message = "Enter a valid first name")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z0-9\\s]{5,}$", message = "Enter a valid last name")
    private String lastName;

    @Email(message = "Enter a valid email")
    private String email;

    @Pattern(regexp = "^[0-9]{10,}$", message = "Enter a valid email")
    private String phone;

    private String accountStatus;

    @Pattern(regexp = "(STUDENT|LECTURER)", message = "Select a course level")
    private String accountType;

    @JsonIgnore
    private String password;

    private String photoUrl;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
