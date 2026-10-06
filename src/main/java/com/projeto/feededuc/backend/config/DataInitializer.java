package com.projeto.feededuc.backend.config;

import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.model.TipoUsuario;
import com.projeto.feededuc.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializa dados padrão no banco de dados ao iniciar a aplicação.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existe um usuário admin
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setLogin("admin");
            admin.setCpf("00000000000");
            admin.setNome("Administrador do Sistema");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
            admin.setPrimeiroAcesso(false);
            
            usuarioRepository.save(admin);
            System.out.println("✅ Usuário admin criado: login=admin, senha=admin123");
        }
    }
}
