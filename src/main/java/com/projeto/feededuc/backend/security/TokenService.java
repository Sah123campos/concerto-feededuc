package com.projeto.feededuc.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.projeto.feededuc.backend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    
    // Chave secreta (deve vir de application.properties)
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("feedback-api") // Quem emitiu o token
                    .withSubject(usuario.getLogin()) // Login do usuário
                    .withClaim("tipoUsuario", usuario.getTipoUsuario().name()) // Adiciona o tipo como claim
                    .withExpiresAt(genExpirationDate()) // Data de expiração
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("feedback-api")
                    .build()
                    .verify(token)
                    .getSubject(); // Retorna o login (subject)
        } catch (JWTVerificationException exception){
            return ""; // Token inválido
        }
    }

    // Define que o token expira em 2 horas
    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
