package com.service.authentication.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String accountStatus;

    private String accountType;

    private String token;

    private String resendToken;

    @JsonIgnore
    private String password;

    private String photoUrl;

    private Timestamp createdAt;

    private Timestamp updatedAt;


}
