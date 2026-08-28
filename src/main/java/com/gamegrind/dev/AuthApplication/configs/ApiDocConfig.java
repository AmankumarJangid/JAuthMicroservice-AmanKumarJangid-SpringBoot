package com.gamegrind.dev.AuthApplication.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
            title = "JAuthMicroservice-by-AmanKumarJangid",
            description = "Generate auth app that can be used with any application",
            contact = @Contact(
                    name = "Aman Kumar Jangid",
                    url = "https://amankumarjangid.gamegrind.dev",
                    email = "amanjangid7847@gmail.com"
            ),
            version = "1.0",
            summary = "This application / microservice is made to make available plug and play authentication in my system " +
                    "without needing to rewrite the entirely new auth application"
    ),
        security = {
            @SecurityRequirement(
                    name = "bearer Authorization"
            )
        }

)

@SecurityScheme(
        name = "bearer Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer", // Authorization: Bearer accesstoken,
        bearerFormat = "JWT"
)
public class ApiDocConfig {

}
