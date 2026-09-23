package com.gamegrind.dev.AuthApplication.services.impl;


import java.security.SecureRandom;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gamegrind.dev.AuthApplication.configs.AppConstants;
import com.gamegrind.dev.AuthApplication.dtos.EmailDto;
import com.gamegrind.dev.AuthApplication.dtos.OtpPurpose;
import com.gamegrind.dev.AuthApplication.exceptions.ResourceNotFoundException;
import com.gamegrind.dev.AuthApplication.services.OtpService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final SecureRandom random = new SecureRandom();

    private final StringRedisTemplate otpRedisTemplate;

    private final KafkaTemplate<String, EmailDto> kafkaEmailTemplate;


    private Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);


    @Override
    public void generateOtp(String email, OtpPurpose purpose) {
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
            logger.error("REDIS CRASHED: ", e);
            // Optional: throw a custom exception so the email step gets skipped on database errors
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
