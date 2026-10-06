package com.projeto.feededuc.backend.controller;

import com.projeto.feededuc.backend.model.Feedback;
import com.projeto.feededuc.backend.model.Resposta;
import com.projeto.feededuc.backend.model.TipoUsuario;
import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.model.StatusFeedback;
import com.projeto.feededuc.backend.repository.UsuarioRepository;
import com.projeto.feededuc.backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * 🔓 Endpoint para TODOS os usuários autenticados
     * Retorna APENAS feedbacks públicos para o mural
     * (Este endpoint é usado pelo "Mural Público")
     */
    @GetMapping
    public ResponseEntity<List<Feedback>> listarFeedbacks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        
        System.out.println("📡 GET /api/feedbacks solicitado por: " + usuarioLogado.getLogin() + " (Role: " + usuarioLogado.getTipoUsuario().name() + ")");
        
        // 🌐 Retorna APENAS feedbacks públicos para o mural
        List<Feedback> feedbacks = feedbackService.listarFeedbacksPublicos();
        System.out.println("🌐 Feedbacks públicos para mural: " + feedbacks.size());
        
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * 🔍 Listar apenas os feedbacks do usuário logado
     */
    @GetMapping("/meus-feedbacks")
    public ResponseEntity<List<Feedback>> listarMeusFeedbacks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        
        System.out.println("🔍 Buscando feedbacks do usuário: " + usuarioLogado.getLogin());
        List<Feedback> feedbacks = feedbackService.listarPorAluno(usuarioLogado.getLogin());
        System.out.println("✅ Feedbacks encontrados para " + usuarioLogado.getLogin() + ": " + feedbacks.size());
    
        return ResponseEntity.ok(feedbacks);
}

    /**
     * 🌐 Listar apenas feedbacks públicos
     */
    @GetMapping("/publicos")
    public ResponseEntity<List<Feedback>> listarFeedbacksPublicos() {
        List<Feedback> feedbacks = feedbackService.listarFeedbacksPublicos();
        System.out.println("🌐 Feedbacks públicos: " + feedbacks.size());
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * 📥 Listar feedbacks recebidos (privados direcionados ao usuário logado)
     */
    @GetMapping("/recebidos")
    public ResponseEntity<List<Feedback>> listarFeedbacksRecebidos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        List<Feedback> feedbacks = feedbackService.listarPorDestinatarioEspecifico(usuarioLogado.getLogin());
        System.out.println("📥 Feedbacks recebidos para " + usuarioLogado.getLogin() + ": " + feedbacks.size());
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * ➕ Criar novo feedback
     */
    @PostMapping
    public ResponseEntity<Feedback> criarFeedback(@RequestBody Feedback feedback) {
        try {
            // Obter usuário logado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof Usuario)) {
                System.out.println("🔒 Tentativa de criar feedback sem usuário autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            
            // Associar o feedback ao usuário logado
            feedback.setAlunoId(usuarioLogado.getLogin());
            
            Feedback feedbackSalvo = feedbackService.salvar(feedback);
            System.out.println("✅ Novo feedback criado por " + usuarioLogado.getLogin() + 
                             ": " + feedback.getTitulo() + 
                             " | Público: " + feedback.isPublico() +
                             " | Dest. Específico: " + feedback.getDestinatarioEspecifico());
            
            return ResponseEntity.ok(feedbackSalvo);
        } catch (Exception e) {
            System.out.println("❌ Erro ao criar feedback: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 💬 Responder a um feedback
     */
    // NO FeedbackController.java - ATUALIZAR método responderFeedback
@PostMapping("/{feedbackId}/responder")
public ResponseEntity<?> responderFeedback(
        @PathVariable Long feedbackId,
        @RequestBody RespostaRequest respostaRequest) {
    
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        
        // Buscar o feedback
        Feedback feedback = feedbackService.buscarPorId(feedbackId);
        if (feedback == null) {
            return ResponseEntity.notFound().build();
        }

        // 🔥 CORREÇÃO: VERIFICAR PERMISSÃO PARA RESPONDER
        if (!podeResponderFeedback(feedback, usuarioLogado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Você não tem permissão para responder este feedback");
        }

        // Criar nova resposta e adicionar à lista de respostas
        Resposta resposta = new Resposta();
        resposta.setConteudo(respostaRequest.getConteudo());
        resposta.setResponsavelId(usuarioLogado.getLogin());
        resposta.setResponsavelNome(usuarioLogado.getNome());
        resposta.setResponsavelTipo(usuarioLogado.getTipoUsuario().name());
        resposta.setNovoStatus(respostaRequest.getNovoStatus());
        resposta.setDataResposta(LocalDateTime.now());

        // Atualizar status do feedback se fornecido
        if (respostaRequest.getNovoStatus() != null) {
            feedback.setStatus(respostaRequest.getNovoStatus());
        } else {
            // Status padrão após resposta
            feedback.setStatus(StatusFeedback.EM_ANALISE);
        }

        // Adicionar resposta ao feedback (cascade salvará a resposta)
        feedback.addResposta(resposta);

        Feedback feedbackAtualizado = feedbackService.salvar(feedback);
        
        System.out.println("💬 Resposta adicionada ao feedback " + feedbackId + 
                         " por " + usuarioLogado.getLogin() +
                         " | Novo status: " + feedback.getStatus());
        
        return ResponseEntity.ok(feedbackAtualizado);

    } catch (Exception e) {
        System.out.println("❌ Erro ao responder feedback: " + e.getMessage());
        return ResponseEntity.badRequest().body("Erro ao responder feedback: " + e.getMessage());
    }
}

// 🔥 MÉTODO AUXILIAR PARA VERIFICAR PERMISSÃO DE RESPOSTA
private boolean podeResponderFeedback(Feedback feedback, Usuario usuario) {
        // Não permitir respostas se já estiver resolvido
        if (feedback.getStatus() == StatusFeedback.RESOLVIDO) {
            return false;
        }

        // Administrador pode responder qualquer feedback
        if (usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
            return true;
        }

        // O autor (quem enviou) pode responder enquanto não estiver resolvido
        if (feedback.getAlunoId() != null && feedback.getAlunoId().equals(usuario.getLogin())) {
            return true;
        }

        // Se houver destinatário específico, apenas ele pode responder
        if (feedback.getDestinatarioEspecifico() != null &&
            !feedback.getDestinatarioEspecifico().isEmpty()) {
            return feedback.getDestinatarioEspecifico().equals(usuario.getLogin());
        }

        // Caso contrário, qualquer usuário do mesmo tipo/grupo pode responder
        return usuario.getTipoUsuario().equals(feedback.getDestinatario());
}

    /**
     * 🔄 Alterar status do feedback
     */
    @PutMapping("/{feedbackId}/status")
    public ResponseEntity<?> alterarStatus(
            @PathVariable Long feedbackId,
            @RequestBody StatusRequest statusRequest) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            
            Feedback feedback = feedbackService.buscarPorId(feedbackId);
            if (feedback == null) {
                return ResponseEntity.notFound().build();
            }

            // 🔐 Verificar permissão para alterar status
            if (!podeAlterarStatus(feedback, usuarioLogado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não tem permissão para alterar o status deste feedback");
            }

            feedback.setStatus(statusRequest.getStatus());
            Feedback feedbackAtualizado = feedbackService.salvar(feedback);
            
            System.out.println("🔄 Status do feedback " + feedbackId + 
                             " alterado para " + statusRequest.getStatus() + 
                             " por " + usuarioLogado.getLogin());
            
            return ResponseEntity.ok(feedbackAtualizado);

        } catch (Exception e) {
            System.out.println("❌ Erro ao alterar status: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao alterar status: " + e.getMessage());
        }
    }

    

    /**
     * 🔐 Verificar se usuário pode alterar status
     */
    private boolean podeAlterarStatus(Feedback feedback, Usuario usuario) {
        // Administrador sempre pode alterar
        if (usuario.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) return true;

        // Se houver destinatário específico, apenas ele pode alterar o status
        if (feedback.getDestinatarioEspecifico() != null && !feedback.getDestinatarioEspecifico().isEmpty()) {
            return feedback.getDestinatarioEspecifico().equals(usuario.getLogin());
        }

        // Usuários pertencentes ao tipo destinatário (por exemplo, professores do mesmo setor)
        // podem alterar o status
        return usuario.getTipoUsuario().equals(feedback.getDestinatario());
    }

    /**
     * ❌ Deletar feedback (apenas admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFeedback(@PathVariable Long id) {
        try {
            // Verificar se é admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
            
            if (!usuarioLogado.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            feedbackService.deletar(id);
            System.out.println("🗑️ Feedback deletado: " + id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("❌ Erro ao deletar feedback: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ✏️ Editar conteúdo do feedback (apenas autor ou admin)
     */
    @PutMapping("/{feedbackId}")
    public ResponseEntity<?> editarFeedback(@PathVariable Long feedbackId, @RequestBody Feedback feedbackRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

            Feedback feedback = feedbackService.buscarPorId(feedbackId);
            if (feedback == null) return ResponseEntity.notFound().build();

            // Somente o autor (quem enviou) ou administrador pode editar o conteúdo
            if (!usuarioLogado.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR) &&
                (feedback.getAlunoId() == null || !feedback.getAlunoId().equals(usuarioLogado.getLogin()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Somente o autor do feedback (ou administrador) pode editar esta mensagem");
            }

            // Atualizar campos permitidos
            if (feedbackRequest.getTitulo() != null) feedback.setTitulo(feedbackRequest.getTitulo());
            if (feedbackRequest.getConteudo() != null) feedback.setConteudo(feedbackRequest.getConteudo());

            Feedback salvo = feedbackService.salvar(feedback);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            System.out.println("❌ Erro ao editar feedback: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao editar feedback: " + e.getMessage());
        }
    }

    /**
     * ✏️ Editar uma resposta existente (apenas autor da resposta ou admin)
     */
    @PutMapping("/{feedbackId}/respostas/{respostaId}")
    public ResponseEntity<?> editarResposta(@PathVariable Long feedbackId,
                                            @PathVariable Long respostaId,
                                            @RequestBody RespostaRequest respostaRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

            Feedback feedback = feedbackService.buscarPorId(feedbackId);
            if (feedback == null) return ResponseEntity.notFound().build();

            Resposta alvo = null;
            for (Resposta r : feedback.getRespostas()) {
                if (r.getId() != null && r.getId().equals(respostaId)) {
                    alvo = r;
                    break;
                }
            }

            if (alvo == null) return ResponseEntity.notFound().build();

            // Apenas admin ou autor da resposta pode editar
            if (!usuarioLogado.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR) &&
                (alvo.getResponsavelId() == null || !alvo.getResponsavelId().equals(usuarioLogado.getLogin()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Somente o autor da resposta (ou administrador) pode editar esta resposta");
            }

            // Atualizar conteúdo
            if (respostaRequest.getConteudo() != null) alvo.setConteudo(respostaRequest.getConteudo());
            if (respostaRequest.getNovoStatus() != null) {
                alvo.setNovoStatus(respostaRequest.getNovoStatus());
                feedback.setStatus(respostaRequest.getNovoStatus());
            }
            alvo.setDataResposta(LocalDateTime.now());

            Feedback salvo = feedbackService.salvar(feedback);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            System.out.println("❌ Erro ao editar resposta: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erro ao editar resposta: " + e.getMessage());
        }
    }
}

// 🔥 DTO para request de resposta
class RespostaRequest {
    private String conteudo;
    private StatusFeedback novoStatus;

    // Getters e Setters
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    
    public StatusFeedback getNovoStatus() { return novoStatus; }
    public void setNovoStatus(StatusFeedback novoStatus) { this.novoStatus = novoStatus; }
}

// 🔥 DTO para request de status
class StatusRequest {
    private StatusFeedback status;

    // Getters e Setters
    public StatusFeedback getStatus() { return status; }
    public void setStatus(StatusFeedback status) { this.status = status; }
}