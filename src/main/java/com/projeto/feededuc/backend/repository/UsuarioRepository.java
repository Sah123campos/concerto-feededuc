package com.projeto.feededuc.backend.repository;

import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.model.TipoUsuario; // 🔥 ADICIONAR
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo login (Matrícula/CPF).
     */
    Optional<Usuario> findByLogin(String login);
    
    /**
     * Verifica se já existe um usuário com o CPF fornecido.
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se já existe um usuário com o login fornecido.
     */
    boolean existsByLogin(String login);

    /**
     * Busca usuários por tipo de usuário
     */
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    /**
     * Busca usuários por nome (contendo o texto, case insensitive)
     */
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se existe usuário com ID diferente mas mesmo login
     */
    boolean existsByLoginAndIdNot(String login, Long id);

    /**
     * Verifica se existe usuário com ID diferente mas mesmo CPF
     */
    boolean existsByCpfAndIdNot(String cpf, Long id);
}