package com.gamegrind.dev.AuthApplication.controllers;

import com.gamegrind.dev.AuthApplication.dtos.OtpPurpose;
import com.gamegrind.dev.AuthApplication.services.OtpService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {



    public record SendOtpRequest( String email ,OtpPurpose purpose ){};
    public record VerifyOtpRequest( String email , String otp, OtpPurpose purpose){}
    public record VerifiedOtpResponse( Integer status , Boolean verified , OtpPurpose purpose){};

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public Boolean getOtp( @RequestBody SendOtpRequest otpRequest){
        otpService.generateOtp( otpRequest.email() , otpRequest.purpose);
        return true;
    }


    @PostMapping("/verify")
    public ResponseEntity<VerifiedOtpResponse> VerifyOtp(@RequestBody VerifyOtpRequest otpRequest){
        Boolean verified = otpService.verifyOtp(otpRequest.email(), otpRequest.otp(), otpRequest.purpose);
        return ResponseEntity.ok(new VerifiedOtpResponse( 200 , verified , otpRequest.purpose));
    }
}
