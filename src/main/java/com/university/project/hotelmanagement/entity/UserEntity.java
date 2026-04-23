package com.university.project.hotelmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String password;
    private String role;

    @Column(nullable = false)
    private boolean enabled = false;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Hotel> hotels;

    @Column(unique = true)
    private String otp;
    private LocalDateTime otpExpiry;
    private int otpAttempts;
    private LocalDateTime otpBlockedUntil;
    private boolean ResetAllowed;
}

