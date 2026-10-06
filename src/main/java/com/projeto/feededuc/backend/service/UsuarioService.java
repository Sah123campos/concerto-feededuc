package com.projeto.feededuc.backend.service;

import com.projeto.feededuc.backend.model.TipoUsuario;
import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Camada de serviço responsável pela lógica de negócio dos Usuários.
 * Refletindo os campos (login, cpf, nome, tipoUsuario, primeiroAcesso)
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Injeção de Dependência via Construtor
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra um novo usuário no sistema com uma senha padrão.
     * O campo 'primeiroAcesso' é automaticamente definido como true no Model.
     * @param novoUsuario Objeto Usuario a ser salvo.
     * @return O usuário salvo no banco de dados.
     * @throws IllegalArgumentException se o login ou CPF já existirem.
     */
    @Transactional
    public Usuario registrarNovoUsuario(Usuario novoUsuario) {
        
        // 1. Validação de duplicidade
        if (novoUsuario.getLogin() != null && usuarioRepository.existsByLogin(novoUsuario.getLogin().trim())) {
            throw new IllegalArgumentException("Login (Matrícula/CPF) já cadastrado.");
        }
        if (novoUsuario.getCpf() != null && usuarioRepository.existsByCpf(novoUsuario.getCpf().trim())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }
        
        // 2. Definir senha: usa a senha fornecida no payload quando presente,
        // caso contrário usa a senha padrão. Em seguida aplica o PasswordEncoder
        // (pode ser NoOpPasswordEncoder em ambiente de teste).
        String senhaFornecida = novoUsuario.getSenha();
        if (senhaFornecida == null || senhaFornecida.trim().isEmpty()) {
            senhaFornecida = "mudar123";
        }
        novoUsuario.setSenha(passwordEncoder.encode(senhaFornecida));
        // Embora o construtor defina como true, garantimos
        novoUsuario.setPrimeiroAcesso(true); 

        // 3. Normalizar dados
        if (novoUsuario.getLogin() != null) {
            novoUsuario.setLogin(novoUsuario.getLogin().trim());
        }
        if (novoUsuario.getCpf() != null) {
             // Limpeza básica do CPF (remover pontos e traços se aplicável na sua regra de negócio)
             novoUsuario.setCpf(novoUsuario.getCpf().replaceAll("[^0-9]", "").trim());
        }
        
        // 4. Salvar no banco de dados
        return usuarioRepository.save(novoUsuario);
    }
    
    /**
     * Busca todos os usuários.
     * @return Uma lista de todos os usuários.
     */
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar todos os usuários (alias para buscarTodos)
     */
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca um usuário pelo seu ID.
     * @param id O ID (Long) do usuário.
     * @return Um Optional contendo o usuário, se encontrado.
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Buscar usuário por ID (versão sem Optional)
     */
    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
    }

    /**
     * Atualiza os dados de perfil de um usuário (apenas nome, login e tipoUsuario).
     * Nota: Este método NÃO altera a senha ou o status de primeiro acesso.
     * @param id O ID do usuário a ser atualizado.
     * @param usuarioDetalhes O objeto com os novos dados.
     * @return O usuário atualizado.
     * @throws IllegalArgumentException se o usuário não for encontrado ou se houver duplicidade de login/cpf.
     */
    @Transactional
    public Usuario atualizarPerfilUsuario(Long id, Usuario usuarioDetalhes) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        // 1. Atualiza Nome e Tipo de Usuário
        usuarioExistente.setNome(usuarioDetalhes.getNome());
        usuarioExistente.setTipoUsuario(usuarioDetalhes.getTipoUsuario());
        
        // 2. Atualiza Login, verificando duplicidade se o valor mudou
        String novoLogin = usuarioDetalhes.getLogin().trim();
        if (!usuarioExistente.getLogin().equals(novoLogin)) {
            if (usuarioRepository.existsByLogin(novoLogin)) {
                throw new IllegalArgumentException("Login já cadastrado por outro usuário.");
            }
            usuarioExistente.setLogin(novoLogin);
        }
        
        // 3. Atualiza CPF, verificando duplicidade se o valor mudou
        String novoCpf = usuarioDetalhes.getCpf().replaceAll("[^0-9]", "").trim();
        if (!usuarioExistente.getCpf().equals(novoCpf)) {
            if (usuarioRepository.existsByCpf(novoCpf)) {
                throw new IllegalArgumentException("CPF já cadastrado por outro usuário.");
            }
            usuarioExistente.setCpf(novoCpf);
        }

        return usuarioRepository.save(usuarioExistente);
    }
    
    /**
     * Atualiza a senha do usuário e marca 'primeiroAcesso' como false.
     * @param id O ID do usuário.
     * @param novaSenha A nova senha em texto puro.
     * @return O usuário atualizado.
     */
    @Transactional
    public Usuario atualizarSenha(Long id, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));
            
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setPrimeiroAcesso(false); // Marca como acesso concluído após mudança de senha
        
        return usuarioRepository.save(usuario);
    }

    /**
     * Trocar senha do usuário com validação da senha atual
     * @param usuarioId ID do usuário
     * @param senhaAtual Senha atual para validação
     * @param novaSenha Nova senha
     */
    @Transactional
    public void trocarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        // Verificar se a senha atual está correta
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        
        // Validar nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres");
        }
        
        // Criptografar e salvar nova senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setPrimeiroAcesso(false); // Marcar que não é mais primeiro acesso
        
        usuarioRepository.save(usuario);
    }

    /**
     * Deleta um usuário pelo seu ID.
     * @param id O ID (Long) do usuário a ser deletado.
     */
    @Transactional
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Busca usuário por login
     * @param login Login do usuário
     * @return Optional com o usuário encontrado
     */
    public Optional<Usuario> buscarPorLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    /**
     * Verifica se existe usuário com o login
     * @param login Login a verificar
     * @return true se existir
     */
    public boolean existePorLogin(String login) {
        return usuarioRepository.existsByLogin(login);
    }

    /**
     * Verifica se existe usuário com o CPF
     * @param cpf CPF a verificar
     * @return true se existir
     */
    public boolean existePorCpf(String cpf) {
        return usuarioRepository.existsByCpf(cpf);
    }

    /**
     * Busca usuários por tipo
     * @param tipoUsuario Tipo de usuário
     * @return Lista de usuários do tipo especificado
     */
    public List<Usuario> buscarPorTipo(String tipoUsuario) {
         try {
            // 🔥 CONVERTER String para Enum
            TipoUsuario tipo = TipoUsuario.valueOf(tipoUsuario.toUpperCase());
            return usuarioRepository.findByTipoUsuario(tipo);
        } catch (IllegalArgumentException e) {
            // Tipo não encontrado, retornar lista vazia
            System.out.println("⚠️ Tipo de usuário não encontrado: " + tipoUsuario);
            return List.of();
        }
    }
}