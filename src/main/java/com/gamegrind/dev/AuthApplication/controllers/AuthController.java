package com.gamegrind.dev.AuthApplication.controllers;

import com.gamegrind.dev.AuthApplication.dtos.*;
import com.gamegrind.dev.AuthApplication.entities.RefreshToken;
import com.gamegrind.dev.AuthApplication.entities.User;
import com.gamegrind.dev.AuthApplication.repositories.RefreshTokenRepository;
import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import com.gamegrind.dev.AuthApplication.security.CookieService;
import com.gamegrind.dev.AuthApplication.security.JwtService;
import com.gamegrind.dev.AuthApplication.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final CookieService cookieService;
    private final StringRedisTemplate otpRedisTemplate;

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<TokenResponse> refreshToken(
//            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ){
        String refreshToken = readRefreshTokenFromRequest(body , request).orElseThrow( ()-> new BadCredentialsException("Refresh token is missing"));

        if( !jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid refresh token type");
        }

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByJti(jti).orElseThrow( ()-> new BadCredentialsException("Refresh token not found in database"));

        if( storedToken.isRevoked()){
            throw new BadCredentialsException("Refresh token is revoked or expired");
        }

        if( storedToken.getExpiresAt().isBefore(Instant.now())){
            throw new BadCredentialsException("Refresh token is expired");
        }

        if(storedToken.getUser().getId() == null || !storedToken.getUser().getId().equals(userId)){
            throw new BadCredentialsException("Refresh token user mismatch");
        }

        // refresh token rotate
        storedToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        var newRefreshTokenObject = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenObject);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user , newJti);

        cookieService.attachRefreshCookie(response, newRefreshToken, jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlseconds(), modelMapper.map(user, UserDto.class));
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {

        //1. from cookie
        if( request.getCookies() != null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter( c-> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v->!v.isBlank())
                    .findFirst();


            if( fromCookie.isPresent() ){
                return fromCookie;
            }
        }

        //2. from body
        if( body != null && body.refreshToken() != null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }

        //3. from custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if( refreshHeader != null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        //4. Authorization = Bearer <Token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if( authHeader != null && !authHeader.isBlank()){
            String candidate = authHeader.substring("Bearer ".length()).trim();
            if( !candidate.isEmpty()){
                try{
                    if(jwtService.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }catch(Exception ignored){

                }
            }
        }

        return Optional.empty();
    }


    @PostMapping("/register")
    @Transactional
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        // Implement registration logic here
        String redisKey = String.format("%s:verified:%s",
                OtpPurpose.REGISTRATION.name().toLowerCase(),
                userDto.getEmail());

        String isVerified = (String) otpRedisTemplate.opsForValue().get(redisKey);

        if(  !"true".equalsIgnoreCase(isVerified)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        otpRedisTemplate.delete(redisKey);
        // 3. Register the user
        UserDto registeredUser = authService.registerUser(userDto);

        // 4. THE FINAL SECURITY LAYER: Delete key to prevent reuse/replay attacks


        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse>  loginUser(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){
        
        Authentication authenticate =  authenticate(loginRequest);

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow( ()-> new BadCredentialsException("Invalid Username and Password"));

        if(!user.isEnable()){
            throw new DisabledException("User is not enabled");
        }

        // generate refresh-token

        String jti = UUID.randomUUID().toString();
        var refreshTokenObject = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        // refresh Token saved
        refreshTokenRepository.save(refreshTokenObject);

        // generate access-token
        String accessToken = jwtService.generateAccessToken(user);
        // generate refresh-token
        String refreshToken = jwtService.generateRefreshToken(user, jti); // jti from the current refreshTokenObject

        // use cookie servie to attach refresh token in cookie
        cookieService.attachRefreshCookie(response, refreshToken, jwtService.getRefreshTtlSeconds());
//        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlseconds(), modelMapper.map(user, UserDto.class));
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        readRefreshTokenFromRequest(body , request).ifPresent( token -> {
            try{
                if( jwtService.isRefreshToken(token)){
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent( storedToken -> {
                        storedToken.setRevoked(true);
                        refreshTokenRepository.save(storedToken);
                    });
                }

            }
            catch (JwtException ignored){
                // log the error
            }
        });

        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();


    }
    // Authenticates the user details before reaching the applicaiton
    private Authentication authenticate(LoginRequest loginRequest) {
        try{
             return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
        }
        catch( Exception e){
            throw new BadCredentialsException("Invalid username or password");
        }
    }


}
