package com.service.authentication.Verification;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationModel {

    @Id
    @NotBlank(message = "This field is required")
    private String email;


    @Pattern(regexp = "^[0-9]{5}$", message = "Numbers only and not more than 5")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String verificationCode;


//    @NotEmpty(message = "Token must not be empty")
    private String token;

}
