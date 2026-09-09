package com.pack.utils;

import com.pack.dto.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RestClient restClient;

    @Value("${msg91.auth-key}")
    private String authKey;

    @Value("${msg91.template-id}")
    private String templateId;

    public String sendOtp(String mobile) {

        String url =
                "https://control.msg91.com/api/v5/otp"
                        + "?template_id=" + templateId
                        + "&mobile=91" + mobile;
//
//        return restClient.post()
//                .uri(url)
//                .header("authkey", authKey)
//                .retrieve()
//                .body(String.class);

        return "OTP send successfully";
    }

    public boolean verifyOtp(String mobile, String otp) {

        String url =
                "https://control.msg91.com/api/v5/otp/verify"
                        + "?mobile=91" + mobile
                        + "&otp=" + otp;

//        String response = restClient.get()
//                .uri(url)
//                .header("authkey", authKey)
//                .retrieve()
//                .body(String.class);

       // return response.contains("success");
        /*
        * Testing Purpose
        * */

        return true;

    }
}