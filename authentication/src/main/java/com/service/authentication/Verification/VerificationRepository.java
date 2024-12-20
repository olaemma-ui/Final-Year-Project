package com.service.authentication.Verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationModel, String> {

    @Query("SELECT u FROM VerificationModel u WHERE u.resendToken=:resendToken")
    Optional<VerificationModel> findByResendToken(String resendToken);

}
