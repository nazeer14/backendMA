package com.pack.controller;

import com.pack.dto.ApiResponse;

import com.pack.dto.JwtResponse;
import com.pack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOtp(
            @RequestParam String mobile) {

        authService.sendOtp(mobile);

        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<JwtResponse> verifyOtp(
            @RequestParam String mobile,
            @RequestParam String otp) {

        JwtResponse response =
                authService.verifyOtpAndLogin(mobile, otp);

        return ResponseEntity.ok(
                response);
    }
}