package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.LoginRequest;
import com.university.project.hotelmanagement.dto.RegisterRequest;
import com.university.project.hotelmanagement.dto.ResetPasswordRequest;
import com.university.project.hotelmanagement.dto.SendOtpRequest;
import com.university.project.hotelmanagement.entity.Otp;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.*;
import com.university.project.hotelmanagement.repository.OtpRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import com.university.project.hotelmanagement.util.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final RateLimitService rateLimitService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, OtpService otpService, RateLimitService rateLimitService, OtpRepository otpRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.rateLimitService = rateLimitService;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    private UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setEnabled(false);

        userRepository.save(user);

        return otpService.sendOtp(request.getEmail(),user.getFirstName(),"REGISTRATION");
    }




    public void confirmSignup(String token, String enteredOtp) {

        Otp otp = otpService.verifyOtp(token, enteredOtp);

        UserEntity user = userRepository.findByEmail(otp.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(),user.getFirstName());

    }


    public String resendOtp(SendOtpRequest request, String purpose) {
        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("REGISTRATION".equalsIgnoreCase(purpose) && user.isEnabled()) {
            throw new OtpException("Account already verified");
        }
        rateLimitService.checkLimit(email);
        return otpService.sendOtp(email, user.getFirstName(), purpose);
    }


    @Scheduled(fixedRate = 300000)
    @Transactional
    public void deleteUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now();
        userRepository.deleteByEnabledFalse(cutoff);
    }



    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            return jwtUtil.generateToken(authentication);

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AccountNotVerifiedException("Account is not verified. Please verify your OTP first.");
        } catch (LockedException e) {
            throw new RuntimeException("Your account is locked. Please contact support.");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public UserEntity update(RegisterRequest request) {
        UserEntity user = getCurrentUser();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }


    public void delete(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!user.getEmail().equals(loggedInUserEmail)) {
            throw new UnauthorizedAccessException("You can only delete your own account!");
        }
        userRepository.deleteById(id);
    }



    public String forgotPassword(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

       return otpService.sendOtp(email,user.getFirstName(), "FORGOT_PASSWORD");
    }


    public void passwordResetConfirm(String token, String enteredOtp) {

        Otp otp = otpService.verifyOtp(token, enteredOtp);

        UserEntity user = userRepository.findByEmail(otp.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
    }



    public void resetPassword(ResetPasswordRequest request) {

        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new BadRequestException("Password must be at least 8 characters and include digit, upper, lower, and special char.");
        }

        Otp otpEntry = otpRepository.findByOtpToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired session token"));

        if (!otpEntry.isVerified()) {
            throw new BadRequestException("OTP verification required before resetting password");
        }

        UserEntity user = userRepository.findByEmail(otpEntry.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.delete(otpEntry);
    }

}