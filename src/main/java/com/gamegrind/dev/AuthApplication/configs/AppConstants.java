package com.gamegrind.dev.AuthApplication.configs;

import com.gamegrind.dev.AuthApplication.dtos.OtpPurpose;

public class AppConstants {
    public static final String[] API_PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/otp/**"
    };

    public static String getOtpTemplate(String otp, OtpPurpose purpose) {
        // 1. Convert the enum to a human-readable action sentence
        String actionDescription = switch (purpose) {
            case REGISTRATION -> "complete your registration";
            case PASSWORD_RESET -> "reset your account password";
            case TWO_FACTOR_AUTH -> "complete your two-factor authentication login";
        };

        return """
        <!DOCTYPE html>
        <html>
        <body style="margin: 0; background-color: #f4f6f8; font-family: sans-serif;">
            <table align="center" width="100%" style="max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); overflow: hidden; border-collapse: collapse;">
                <tr>
                    <td style="background-color: #4F46E5; padding: 25px; text-align: center; color: #ffffff; font-size: 22px; font-weight: bold;">
                        App Security
                    </td>
                </tr>
                <tr>
                    <td style="padding: 40px 30px; text-align: center;">
                        <h2 style="color: #111827; margin-bottom: 10px;">Verify Your Identity</h2>
                        <p style="color: #4B5563; font-size: 15px;">Use the verification code below to [ACTION_TEXT]. Do not share this code with anyone.</p>
                        
                        <div style="margin: 30px auto; padding: 15px 35px; display: inline-block; background-color: #F3F4F6; border: 2px dashed #4F46E5; border-radius: 8px;">
                            <span style="font-size: 38px; font-weight: 800; letter-spacing: 5px; color: #4F46E5; font-family: monospace;">[OTP_CODE]</span>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td style="background-color: #F9FAFB; padding: 20px; text-align: center; color: #9CA3AF; font-size: 12px; border-top: 1px solid #E5E7EB;">
                        © 2026 GameGrind. All rights reserved.
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """
                .replace("[ACTION_TEXT]", actionDescription)
                .replace("[OTP_CODE]", otp);
    }
}
