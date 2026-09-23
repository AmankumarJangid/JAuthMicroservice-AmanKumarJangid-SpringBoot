package com.gamegrind.dev.AuthApplication.exceptions;

import com.gamegrind.dev.AuthApplication.dtos.ErrorResponse;
import io.jsonwebtoken.JwtException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // resource not found exception handler :: method
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
        ErrorResponse notFoundError =  new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND, 404 );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
        ErrorResponse illegalArgumentError =  new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, 400 );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentError);
    }

    @ExceptionHandler({JwtException.class , BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleJWTException(JwtException exception){
        ErrorResponse jwtError = new ErrorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED, 401);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwtError);
    }
}
