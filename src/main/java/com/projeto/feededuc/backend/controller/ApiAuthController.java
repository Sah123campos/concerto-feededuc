package com.projeto.feededuc.backend.controller;

import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.repository.UsuarioRepository;
import com.projeto.feededuc.backend.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ApiAuthController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authorization header missing"));
            }

            // Caso 1: Bearer JWT
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.replace("Bearer ", "");
                String login = tokenService.validateToken(token);

                if (login == null || login.isBlank()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Token inválido ou expirado"));
                }

                var usuarioOpt = usuarioRepository.findByLogin(login);
                if (usuarioOpt.isEmpty()) {
                    return ResponseEntity.status(401).body(Map.of("error", "Usuário não encontrado para o token fornecido"));
                }

                Usuario usuario = usuarioOpt.get();

                return ResponseEntity.ok(Map.of(
                    "login", usuario.getLogin(),
                    "nome", usuario.getNome(),
                    "tipoUsuario", usuario.getTipoUsuario().name(),
                    "primeiroAcesso", usuario.isPrimeiroAcesso(),
                    "token", token
                ));
            }

            // Caso 2: Basic Auth (frontend antigo usa Basic header)
            if (authHeader.startsWith("Basic ")) {
                try {
                    String base64 = authHeader.replace("Basic ", "");
                    byte[] decoded = java.util.Base64.getDecoder().decode(base64);
                    String cred = new String(decoded);
                    String[] parts = cred.split(":", 2);
                    if (parts.length != 2) {
                        return ResponseEntity.status(401).body(Map.of("error", "Basic credentials malformed"));
                    }
                    String login = parts[0];
                    String senha = parts[1];

                    // Autentica com AuthenticationManager para validar credenciais
                    Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(login, senha)
                    );

                    Usuario usuario = (Usuario) auth.getPrincipal();
                    String token = tokenService.generateToken(usuario);

                    return ResponseEntity.ok(Map.of(
                        "login", usuario.getLogin(),
                        "nome", usuario.getNome(),
                        "tipoUsuario", usuario.getTipoUsuario().name(),
                        "primeiroAcesso", usuario.isPrimeiroAcesso(),
                        "token", token
                    ));
                } catch (Exception ex) {
                    return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
                }
            }

            return ResponseEntity.status(401).body(Map.of("error", "Authorization header invalid"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao validar token: " + e.getMessage()));
        }
    }
}
