package com.projeto.feededuc.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS para garantir que o frontend possa acessar a API do Spring Boot.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // Aplica a política CORS a todos os caminhos
        registry.addMapping("/**") 
            // Permite acesso de qualquer origem local (localhost, 127.0.0.1, qualquer porta)
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") 
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(false) // false para evitar problemas com CORS
            .maxAge(3600); // Cache por 1 hora
    }
}