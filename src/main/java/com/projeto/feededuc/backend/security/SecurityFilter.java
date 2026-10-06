package com.projeto.feededuc.backend.security;

import com.projeto.feededuc.backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Recupera o token Bearer (se houver)
        var token = this.recoverToken(request);

        // 2. Se o token existir, valida e autentica (JWT)
        if (token != null) {
            var login = tokenService.validateToken(token);
            UserDetails user = usuarioRepository.findByLogin(login).orElse(null);

            if (user != null) {
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            // 3. Se não houver Bearer, verificar se há Basic auth (modo demo/local)
            var authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                try {
                    String base64Credentials = authHeader.substring("Basic ".length()).trim();
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    final String[] values = credentials.split(":", 2);
                    if (values.length == 2) {
                        String login = values[0];
                        String senha = values[1];
                        // Buscar usuário no banco
                        UserDetails user = usuarioRepository.findByLogin(login).orElse(null);
                        if (user != null) {
                            // Passwords stored as plain text in this demo setup (NoOpPasswordEncoder)
                            if (user.getPassword() != null && user.getPassword().equals(senha)) {
                                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        }
                    }
                } catch (Exception e) {
                    // falha no decode — ignorar e continuar
                    System.out.println("⚠️ Falha ao processar Basic auth: " + e.getMessage());
                }
            }
        }
        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        // O token JWT vem no formato "Bearer [token]", removemos o "Bearer "
        return authHeader.replace("Bearer ", "");
    }
}