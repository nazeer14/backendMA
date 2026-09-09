package com.pack.service;

import com.pack.dto.JwtResponse;
import com.pack.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    void sendOtp(String mobile);
    JwtResponse verifyOtpAndLogin(String mobile,String otp);
    User createUser(String mobile);
    void validateMobile(String mobile);
}
