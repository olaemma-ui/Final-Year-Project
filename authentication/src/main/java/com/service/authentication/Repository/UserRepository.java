package com.service.authentication.Repository;

import com.service.authentication.Model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserModel, String> {

    @Query("SELECT u FROM User u WHERE u.email=:email ")
    Optional<UserModel> findByEmail(String email);

//    @Query("SELECT u FROM User u WHERE u.identifier=:identifier")
//    Optional<UserModel> findByIdentifier(String identifier);

    @Query("SELECT u FROM User u WHERE u.phone=:phone OR u.email=:email")
    Optional<UserModel> findByPhoneOrEmail(String phone, String email);
//
//    @Query("SELECT u FROM User u WHERE u.token=:token")
//    Optional<UserModel> findByToken(String token);



    @Query("SELECT u FROM User u WHERE u.accountType=:accountType ")
    List<UserModel> findByAccountType(String accountType);
}
