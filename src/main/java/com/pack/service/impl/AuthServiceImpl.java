package com.pack.service.impl;

import com.pack.dto.JwtResponse;
import com.pack.entity.User;
import com.pack.enums.Role;
import com.pack.exceptions.InvalidOtpException;
import com.pack.repository.UserRepository;
import com.pack.service.AuthService;
import com.pack.utils.JwtUtil;
import com.pack.utils.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Override
    public void sendOtp(String mobile) {

        validateMobile(mobile);

        otpService.sendOtp(mobile);
    }

    @Override
    public JwtResponse verifyOtpAndLogin(
            String mobile,
            String otp) {

        validateMobile(mobile);

        boolean verified =
                otpService.verifyOtp(mobile, otp);

        if (!verified) {
            throw new InvalidOtpException(
                    "Invalid OTP");
        }

        User user = userRepository
                .findByPhone(mobile)
                .orElseGet(() -> createUser(mobile));

        String accessToken =
                jwtUtil.generateToken(
                        user.getId(),
                        user.getPhone(),
                        user.getRole().name());

        String refreshToken =
                jwtUtil.generateRefreshToken(user.getPhone());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .build();
    }

    @Override
    public User createUser(String mobile) {

        User user = new User();

        user.setPhone(mobile);
        user.setRole(Role.USER);
        user.setIsVerified(true);

        return userRepository.save(user);
    }

    @Override
    public void validateMobile(String mobile) {

        if (mobile == null ||
                !mobile.matches("^[6-9]\\d{9}$")) {

            throw new IllegalArgumentException(
                    "Invalid mobile number");
        }
    }
}
