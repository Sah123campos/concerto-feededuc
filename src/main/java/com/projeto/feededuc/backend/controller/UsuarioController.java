package com.projeto.feededuc.backend.controller;

import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.service.UsuarioService;
import com.projeto.feededuc.backend.model.TipoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST para a gestão de Usuários.
 * Rotas: /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService; // 🔥 INJEÇÃO DO SERVICE

    // POST /api/usuarios - Cadastrar novo usuário (APENAS ADMIN)
    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario novoUsuario) {
        try {
            // 🔐 VERIFICAÇÃO DE PERMISSÃO - APENAS ADMIN pode cadastrar
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Usuário não autenticado")
                );
            }
            
            // Verificar se o usuário logado é ADMIN
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            if (!usuarioLogado.getTipoUsuario().name().equals("ADMINISTRADOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Acesso negado. Apenas administradores podem cadastrar usuários.")
                );
            }

            // O serviço trata a criptografia da senha e as validações
            Usuario usuarioSalvo = usuarioService.registrarNovoUsuario(novoUsuario);
            
            // Oculta a senha antes de retornar
            usuarioSalvo.setSenha(null); 
            
            return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            // Retorna 400 Bad Request se houver erro de validação (login/cpf duplicado)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse("Erro de validação: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao tentar cadastrar o usuário: " + e.getMessage())
            );
        }
    }

    /**
     * POST /api/usuarios/register - Cadastro público de usuário.
     * Este endpoint permite que novos usuários se cadastrem no sistema sem
     * necessidade de autenticação. Campos esperados: login, cpf, nome, tipoUsuario (opcional).
     */
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuarioPublico(@RequestBody Usuario novoUsuario) {
        try {
            // Se não vier tipo, assumir ALUNO
            if (novoUsuario.getTipoUsuario() == null) {
                novoUsuario.setTipoUsuario(TipoUsuario.ALUNO);
            }

            Usuario usuarioSalvo = usuarioService.registrarNovoUsuario(novoUsuario);
            usuarioSalvo.setSenha(null);
            return new ResponseEntity<>(usuarioSalvo, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse("Erro de validação: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao cadastrar usuário: " + e.getMessage())
            );
        }
    }

    /**
     * 🔍 Buscar usuários por tipo para destinatários de feedback
     */
    @GetMapping("/destinatarios/{tipoUsuario}")
    public ResponseEntity<?> listarDestinatariosPorTipo(@PathVariable String tipoUsuario) {
        try {
            // 🔥 AGORA usuarioService está disponível
            List<Usuario> usuarios = usuarioService.buscarPorTipo(tipoUsuario);
            
            // Remove senhas antes de retornar
            usuarios.forEach(u -> u.setSenha(null));
            
            // Formatar resposta para frontend
            List<DestinatarioDTO> destinatarios = usuarios.stream()
                .map(usuario -> new DestinatarioDTO(
                    usuario.getLogin(),
                    usuario.getNome(),
                    usuario.getTipoUsuario().name()
                ))
                .collect(Collectors.toList());
            
            System.out.println("👥 Destinatários encontrados para " + tipoUsuario + ": " + destinatarios.size());
            
            return ResponseEntity.ok(destinatarios);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Erro ao buscar destinatários: " + e.getMessage())
            );
        }
    }

    /**
     * 🔍 Buscar todos os usuários para destinatários
     */
    @GetMapping("/destinatarios")
    public ResponseEntity<?> listarTodosDestinatarios() {
        try {
            // Tipos de usuários que podem receber feedbacks
            List<String> tiposDestinatarios = Arrays.asList(
                "PROFESSOR", "COORDENADOR", "SETOR_APOIO", "LIMPEZA", 
                "MANUTENCAO", "CANTINA", "REFEITORIO", "AUDITORIO", "OPP"
            );
            
            Map<String, List<DestinatarioDTO>> destinatarios = tiposDestinatarios.stream()
                .collect(Collectors.toMap(
                    tipo -> tipo,
                    tipo -> {
                        List<Usuario> usuarios = usuarioService.buscarPorTipo(tipo);
                        usuarios.forEach(u -> u.setSenha(null));
                        return usuarios.stream()
                            .map(usuario -> new DestinatarioDTO(
                                usuario.getLogin(),
                                usuario.getNome(),
                                usuario.getTipoUsuario().name()
                            ))
                            .collect(Collectors.toList());
                    }
                ));
            
            System.out.println("👥 Todos destinatários carregados");
            return ResponseEntity.ok(destinatarios);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Erro ao buscar destinatários: " + e.getMessage())
            );
        }
    }

    // PUT /api/usuarios/trocar-senha - Trocar senha do usuário logado
    @PutMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody Map<String, String> senhaData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Usuário não autenticado")
                );
            }
            
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            
            String senhaAtual = senhaData.get("senhaAtual");
            String novaSenha = senhaData.get("novaSenha");
            
            if (senhaAtual == null || novaSenha == null) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Senha atual e nova senha são obrigatórias")
                );
            }
            
            if (novaSenha.length() < 6) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("A nova senha deve ter pelo menos 6 caracteres")
                );
            }
            
            usuarioService.trocarSenha(usuarioLogado.getId(), senhaAtual, novaSenha);
            
            return ResponseEntity.ok(Map.of("message", "Senha trocada com sucesso"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao trocar senha: " + e.getMessage())
            );
        }
    }

    // GET /api/usuarios - Listar usuários (APENAS ADMIN)
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            // 🔐 VERIFICAÇÃO DE PERMISSÃO - APENAS ADMIN pode listar
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Usuário não autenticado")
                );
            }
            
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            if (!usuarioLogado.getTipoUsuario().name().equals("ADMINISTRADOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Acesso negado. Apenas administradores podem listar usuários.")
                );
            }
            
            var usuarios = usuarioService.listarTodosUsuarios();
            
            // Remove as senhas antes de retornar
            usuarios.forEach(usuario -> usuario.setSenha(null));
            
            return ResponseEntity.ok(usuarios);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao listar usuários: " + e.getMessage())
            );
        }
    }

    // GET /api/usuarios/{id} - Buscar usuário por ID (APENAS ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarUsuarioPorId(@PathVariable Long id) {
        try {
            // 🔐 VERIFICAÇÃO DE PERMISSÃO - APENAS ADMIN pode buscar
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Usuário não autenticado")
                );
            }
            
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            if (!usuarioLogado.getTipoUsuario().name().equals("ADMINISTRADOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Acesso negado. Apenas administradores podem buscar usuários.")
                );
            }
            
            var usuario = usuarioService.buscarUsuarioPorId(id);
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("Usuário não encontrado")
                );
            }
            
            // Remove a senha antes de retornar
            usuario.setSenha(null);
            
            return ResponseEntity.ok(usuario);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao buscar usuário: " + e.getMessage())
            );
        }
    }

    // DELETE /api/usuarios/{id} - Deletar usuário (APENAS ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        try {
            // 🔐 VERIFICAÇÃO DE PERMISSÃO - APENAS ADMIN pode deletar
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse("Usuário não autenticado")
                );
            }
            
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            if (!usuarioLogado.getTipoUsuario().name().equals("ADMINISTRADOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("Acesso negado. Apenas administradores podem deletar usuários.")
                );
            }
            
            // Não permitir deletar a si mesmo
            if (usuarioLogado.getId().equals(id)) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Não é possível deletar seu próprio usuário")
                );
            }
            
            usuarioService.deletarUsuario(id);
            
            return ResponseEntity.ok(Map.of("message", "Usuário deletado com sucesso"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Erro interno ao deletar usuário: " + e.getMessage())
            );
        }
    }
}

// 🔥 DTO para resposta formatada de destinatários
class DestinatarioDTO {
    private String login;
    private String nome;
    private String tipo;
    
    public DestinatarioDTO(String login, String nome, String tipo) {
        this.login = login;
        this.nome = nome;
        this.tipo = tipo;
    }
    
    // Getters e Setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

// Classe auxiliar para formatar mensagens de erro padronizadas
class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}