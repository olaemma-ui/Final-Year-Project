package com.service.authentication.Verification;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter

@NoArgsConstructor
@AllArgsConstructor
public class VerificationModel {

    @Id
    @NotBlank(message = "This field is required")
    private String token;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resendToken;

    @Transient
//    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Numbers only and not more than 5")
    private String verificationCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonIgnore
    private String codeHash;

    @JsonIgnore
    private LocalDateTime createdAt;

    @JsonIgnore
    private String verificationType;

}
