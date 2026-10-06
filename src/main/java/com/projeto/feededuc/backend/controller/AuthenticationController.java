package com.projeto.feededuc.backend.controller;

import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String login = loginData.get("login");
            String senha = loginData.get("senha");
            
            var usernamePassword = new UsernamePasswordAuthenticationToken(login, senha);
            Authentication auth = this.authenticationManager.authenticate(usernamePassword);
            
            Usuario usuario = (Usuario) auth.getPrincipal();
            var token = tokenService.generateToken(usuario);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "primeiroAcesso", usuario.isPrimeiroAcesso(),
                "login", usuario.getLogin(),
                "nome", usuario.getNome(),
                "tipoUsuario", usuario.getTipoUsuario().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Credenciais inválidas"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // O logout em JWT é geralmente feito no frontend removendo o token
            // Aqui apenas confirmamos a operação
            return ResponseEntity.ok(Map.of(
                "message", "Logout realizado com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erro ao fazer logout"
            ));
        }
    }
}
