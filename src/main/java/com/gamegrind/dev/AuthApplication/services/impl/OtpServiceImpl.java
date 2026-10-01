package com.gamegrind.dev.AuthApplication.services.impl;


import java.security.SecureRandom;
import java.time.Duration;

import com.gamegrind.dev.AuthApplication.exceptions.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gamegrind.dev.AuthApplication.configs.AppConstants;
import com.gamegrind.dev.AuthApplication.dtos.EmailDto;
import com.gamegrind.dev.AuthApplication.dtos.OtpPurpose;
import com.gamegrind.dev.AuthApplication.exceptions.ResourceNotFoundException;
import com.gamegrind.dev.AuthApplication.services.OtpService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final SecureRandom random = new SecureRandom();

    private final StringRedisTemplate otpRedisTemplate;

    private final KafkaTemplate<String, EmailDto> kafkaEmailTemplate;


    private Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);


    @Override
    public void generateOtp(String email, OtpPurpose purpose) {
        String purposeStr = purpose.name().toLowerCase();
        // Unique key to track the 1-minute cooldown per email and purpose
        String cooldownKey = String.format("%s:otp-cooldown:%s", purposeStr, email);

        // 1. Try to set the cooldown key. If it already exists, setIfAbsent returns false.
        Boolean isAllowed = otpRedisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "lock", Duration.ofMinutes(1)); //By using Redis's setIfAbsent (the SETNX command),
        // you can atomically check if a cooldown key already exists. If it exists

//        Atomic Operation: setIfAbsent combines checking
//        if a key exists and creating it into a single atomic action in Redis.
//        This prevents race conditions if a malicious client fires multiple requests at
//        the exact same millisecond.

        // Handle null or false (means an OTP was already requested within the last 60 seconds)
        if (Boolean.FALSE.equals(isAllowed) || isAllowed == null) {
            logger.warn("OTP request blocked for email: {} due to rate limiting.", email);
            throw new RateLimitException("You can only request one OTP per minute. Please wait.");
        }

        // 2. Proceed with generating and sending the OTP since the cooldown lock was successfully acquired
        String otp = generateRandomOtp(6);

        try {
            otpRedisTemplate.opsForValue().set( String.format("%s:otp:%s", purpose.name().toLowerCase(),  email), otp, Duration.ofMinutes(10));

            EmailDto newEmail = new EmailDto();
            newEmail.setEmail(email);
            newEmail.setSubject("Verification One Time Password for Authentication At GameGrind.dev");

            String message = AppConstants.getOtpTemplate(otp , purpose);
            newEmail.setMessage(message);

            kafkaEmailTemplate.send("email-notification", newEmail)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("====== KAFKA SUCCESS ======");
                            logger.info("Sent message to topic: {} partition: {} offset: {}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            logger.error("====== KAFKA FAILURE ======");
                            logger.error("Failed to publish record to Kafka broker!", ex);
                        }
                    });

            logger.info("Successfully pushed to Redis!");
        } catch (Exception e) {
            // FIX: Clean up the lock from Redis so the user isn't stuck waiting 60s for a systemic error
            try {
                otpRedisTemplate.delete(cooldownKey);
                logger.info("Cleared OTP cooldown lock due to internal failure.");
            } catch (Exception deleteEx) {
                logger.error("Failed to clear cooldown lock key from Redis!", deleteEx);
            }

            logger.error("REDIS/KAFKA CRASHED: ", e);
            throw new RuntimeException("Service temporarily unavailable, please try again.");
        }


        logger.info("\n[OtpService] otp : {} ", otp);
    }

    @Override
    public Boolean verifyOtp(String email, String otp, OtpPurpose purpose) {

        if( email.isEmpty() ) throw new ResourceNotFoundException("Email not found ");
        if( otp.isEmpty() ) throw new ResourceNotFoundException("No Otp provided");

        try{

            String storeOtp = otpRedisTemplate.opsForValue().get(String.format("%s:otp:%s", purpose.name().toLowerCase(), email));

            if( otp.trim().equalsIgnoreCase(storeOtp)){

                otpRedisTemplate.opsForValue().set(String.format("%s:verified:%s", purpose.name().toLowerCase(), email) , "true");
                otpRedisTemplate.delete(email);
                return true ;
            }
            else{

                return false;
            }

        }
        catch(Exception e ){
            throw new RuntimeException("Service temporarily unavailable, please try again.");
        }

        
        
    }

    private String generateRandomOtp(int size){
        StringBuilder sb = new StringBuilder();
        for( int i = 1 ; i <= size ; i++){
            sb.append( random.nextInt(10));
        }

        return sb.toString();
    }
}
