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

    // 1. Resource not found exception handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        ErrorResponse notFoundError = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND, 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundError);
    }

    // 2. Illegal argument handler
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorResponse illegalArgumentError = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST, 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentError);
    }

    // 3. JWT & Bad Credentials handler
    @ExceptionHandler({JwtException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleJWTException(Exception exception) { // Changed parameter to Exception to support both types safely
        ErrorResponse jwtError = new ErrorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED, 401);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jwtError);
    }

    // 4. Rate Limiting Handler (NEW)
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException exception) {
        ErrorResponse rateLimitError = new ErrorResponse(exception.getMessage(), HttpStatus.TOO_MANY_REQUESTS, 429);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(rateLimitError);
    }

    // 5. Generic RuntimeException handler
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception) {
        ErrorResponse error = new ErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 6. Fallback Global Handler for ANY other unhandled Exception (NEW)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUnhandledExceptions(Exception exception) {
        // Log the actual unexpected error stack trace internally for debugging
        // logger.error("Unhandled Exception caught: ", exception);

        ErrorResponse error = new ErrorResponse(
                "An unexpected internal server error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                500
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
