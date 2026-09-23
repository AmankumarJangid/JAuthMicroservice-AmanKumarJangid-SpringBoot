package com.gamegrind.dev.AuthApplication.services;

import com.gamegrind.dev.AuthApplication.dtos.OtpPurpose;
import org.springframework.stereotype.Service;

@Service
public interface OtpService {

    public void generateOtp(String email, OtpPurpose purpose);
    public Boolean verifyOtp(String email, String otp, OtpPurpose purpose);

    

}
