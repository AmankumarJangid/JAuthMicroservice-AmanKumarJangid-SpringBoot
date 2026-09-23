//package com.gamegrind.dev.AuthApplication.configs;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    String[] origins;
//    String[] methods;
//    String[] headers;
//    boolean credentials;
//
//    CorsConfig(
//            @Value("${app.cors.allowed-methods}") String[] methods,
//            @Value("${app.cors.allowed-origins}") String[] origins,
//            @Value("${app.cors.allow-credentials}") boolean credentials,
//            @Value("${app.cors.allowed-headers}") String[] headers
//    ){
//        this.credentials = credentials;
//        this.origins = origins;
//        this.headers = headers;
//        this.methods = methods;
//    }
//
//    @Override
//    public void addCorsMappings(CorsRegistry corsRegistry){
//        corsRegistry.addMapping("/**")
//                .allowedOrigins(origins)
//                .allowCredentials(credentials)
//                .allowedHeaders(headers)
//                .allowedMethods(methods);
//    }
//
//}
