package com.university.project.hotelmanagement.services;
import com.university.project.hotelmanagement.entity.Otp;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.OtpException;
import com.university.project.hotelmanagement.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    public String sendOtp(String email,String username,String type) {

        rateLimitService.checkLimit(email);

        String otp = generateOtp();
        String token = UUID.randomUUID().toString();

        Otp entity = new Otp();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setOtpToken(token);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(1));
        entity.setAttempts(0);
        entity.setVerified(false);

        otpRepository.save(entity);
        if ("FORGOT_PASSWORD".equalsIgnoreCase(type)) {
            emailService.sendForgotPasswordEmail(email, username, otp);
        } else {
            emailService.sendOtpEmail(email, username, otp);
        }
        return token;
    }

    public Otp verifyOtp(String token, String enteredOtp) {

        Otp otp = otpRepository.findByOtpToken(token)
                .orElseThrow(() -> new OtpException("Invalid token"));

        if (otp.isVerified()) {
            throw new OtpException("OTP already used");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP expired");
        }

        if (otp.getAttempts() >= 3) {
            throw new OtpException("Too many attempts");
        }

        if (!otp.getOtp().equals(enteredOtp)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw new OtpException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
        return otp;
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}