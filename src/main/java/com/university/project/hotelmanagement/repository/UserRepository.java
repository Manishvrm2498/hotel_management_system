package com.university.project.hotelmanagement.repository;


import com.university.project.hotelmanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.Optional;

@RequestMapping
public interface UserRepository extends JpaRepository<UserEntity , Long> {

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<UserEntity> findByOtp(String otp);

    void deleteByEnabledFalse(LocalDateTime cutoff);

    Optional<UserEntity> findByEmailAndEnabled(String email, boolean enabled);}
