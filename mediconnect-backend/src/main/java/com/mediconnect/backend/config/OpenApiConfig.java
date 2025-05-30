package com.mediconnect.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI mediConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediConnect API")
                        .description("API de la plateforme de téléconsultation MediConnect")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MediConnect Support")
                                .email("support@mediconnect.com")
                                .url("https://www.mediconnect.com"))
                        .license(new License()
                                .name("MediConnect License")
                                .url("https://www.mediconnect.com/license")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, 
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entrez votre JWT token avec le préfixe Bearer : Bearer <token>")));
    }
} 