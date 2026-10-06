package com.projeto.feededuc.backend.config;

import com.projeto.feededuc.backend.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuração de Segurança com Spring Security usando JWT e banco de dados.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    /**
     * Define o Bean AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Define o Bean PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // WARNING: NoOpPasswordEncoder stores passwords in plain text.
        // This effectively 'removes' password encoding and should ONLY be
        // used for local testing or when explicitly requested.
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Define a fonte de configuração CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite todas as origens para desenvolvimento
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); 
        
        // Permite todos os métodos e cabeçalhos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permite credenciais (cookies, headers de autenticação, etc.)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Aplica a configuração CORS
            .cors(withDefaults())
            // Desabilita CSRF (necessário para APIs REST stateless)
            .csrf(AbstractHttpConfigurer::disable)
            // Configura sessão como STATELESS (JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configura a autorização de requisições
           .authorizeHttpRequests((requests) -> requests
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

    // Enviar feedback exige autenticação (apenas usuários logados podem criar)
    // Removemos a permissão pública para evitar NPE no controller quando não houver auth

    // LIBERAR VALIDAÇÃO DE LOGIN DO FRONT
    .requestMatchers("/api/auth/validate").permitAll()

    // LIBERAR SEU LOGIN E ARQUIVOS
    .requestMatchers(
        "/auth/login",
        "/auth/logout",
        "/api/usuarios",
        "/api/usuarios/register",
        "/", "/*.html",
        "/css/**",
        "/js/**"
    ).permitAll()

    // RESTO CONTINUA PROTEGIDO
    .anyRequest().authenticated()
)

            // Adiciona o filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
