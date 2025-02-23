package com.service.authentication.Signup;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SignUpModel {


    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Enter a valid name")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Enter a valid name")
    private String lastName;

    @NotBlank(message = "This field is required")
    @Email(message = "Enter a valid email address")
    private String email;

//    @NotBlank(message = "This field is required (identifier)")
    private String identifier;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "(STUDENT|LECTURER|ADMIN)", message = "Enter a valid account type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountType;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*]).{8,}$", message = "Password too weak")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @Transient
    private String confirmPassword;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resendToken;

}
