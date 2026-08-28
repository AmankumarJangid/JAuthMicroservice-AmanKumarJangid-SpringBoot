package com.gamegrind.dev.AuthApplication.configs;


import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import com.gamegrind.dev.AuthApplication.security.CustomUserDetailService;
import com.gamegrind.dev.AuthApplication.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private AuthenticationSuccessHandler successHandler;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationSuccessHandler successHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        try{

            http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                    // STATELESS if we don't want store any data server for future user , data is processed in one single sweep
                    // ALWAYS creates a HTTP session to store the current data for while
                    // IF_NEEDED (by default ) create only if its needed
                    // NEVER never create a session but if exist then use it but do not create it
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorizeHttpRequests ->
                            authorizeHttpRequests
                                    // permits all the url in the api public urls
                                    .requestMatchers(AppConstants.API_PUBLIC_URLS).permitAll()
                                    .anyRequest().authenticated()
                    )
                    .oauth2Login(outh2 ->
                            outh2.successHandler(successHandler)
                                    .failureHandler(null)
                            )
                    .logout(AbstractHttpConfigurer::disable)
//                    .httpBasic(Customizer.withDefaults()) // For Base Auth Use httpBasic Authentication
                    .exceptionHandling(
                            ex -> ex.authenticationEntryPoint(
                                    (request,response, e) -> {
                        // error message
                        log.info("Unauthorized access : {}", e.getMessage()); // logger instead of using e.printStackTrace() for better logging and debugging


                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json");
                        String message = "Unauthorized access : " + e.getMessage();
                        Map<String, String> errorMap = Map.of(
                                "message", message,
                                "status" , String.valueOf(HttpStatus.UNAUTHORIZED.value()), // 401
                                "statusCode", Integer.toString(401)
                        );
                        var objectMapper = new ObjectMapper();
                        response.getWriter().write(objectMapper.writeValueAsString(errorMap));

                    }))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//                    .formLogin(Customizer.withDefaults()) // for now we will use http basic authentication, but in future we can use form login or jwt authentication
//                    .csrf(csrf -> csrf.disable())
            ;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
//        return NoOpPasswordEncoder.getInstance(); // only for testing perpose for direct string to string check
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public UserDetailsService users(){
//        UserDetails user = User.builder()
//                .username("user")
//                .password("{noop}password123")
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password("{noop}admin123")
//                .roles("ADMIN", "USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(user, admin);
//    }

}
