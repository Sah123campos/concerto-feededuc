package com.projeto.feededuc.backend.service;

import com.projeto.feededuc.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Esta classe é usada pelo Spring Security para carregar detalhes do usuário
@Service
public class AuthorizationService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AuthorizationService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Encontra o usuário pelo login, senão lança exceção de segurança
        return repository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
    }
}