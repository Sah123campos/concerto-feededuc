package com.projeto.feededuc.backend.service;

import com.projeto.feededuc.backend.model.Feedback;
import com.projeto.feededuc.backend.model.Usuario;
import com.projeto.feededuc.backend.repository.FeedbackRepository;
import com.projeto.feededuc.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    private final UsuarioRepository usuarioRepository; // 🔥 ADICIONE ESTA LINHA

    // 🔥 INJEÇÃO VIA CONSTRUTOR (RECOMENDADO)
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, UsuarioRepository usuarioRepository) {
        this.feedbackRepository = feedbackRepository;
        this.usuarioRepository = usuarioRepository; // 🔥 INICIALIZE
    }

    /**
     * Listar TODOS os feedbacks (apenas para admin)
     */
    public List<Feedback> listarTodos() {
        return feedbackRepository.findAll();
    }

    /**
     * 🔍 Listar feedbacks por aluno
     */
    public List<Feedback> listarPorAluno(String alunoId) {
    System.out.println("🔍 Buscando feedbacks do aluno: " + alunoId);
    List<Feedback> feedbacks = feedbackRepository.findByAlunoId(alunoId);
    System.out.println("✅ Feedbacks encontrados: " + feedbacks.size());
    
    // Debug: mostrar cada feedback encontrado
    feedbacks.forEach(f -> {
        System.out.println("📋 Feedback ID: " + f.getId() + 
                          " | Aluno: " + f.getAlunoId() + 
                          " | Título: " + f.getTitulo());
    });
    
    return feedbacks;
}

    /**
     * 🔍 Listar feedbacks públicos
     */
    public List<Feedback> listarFeedbacksPublicos() {
        return feedbackRepository.findByPublicoTrue();
    }

    /**
     * 🔍 Listar feedbacks visíveis para um usuário
     * (públicos + seus próprios feedbacks + feedbacks onde é destinatário)
     */
    /**
 * 🔍 Listar feedbacks visíveis para um usuário (VERSÃO CORRIGIDA)
 */
public List<Feedback> listarFeedbacksVisiveis(String usuarioLogin) {
    List<Feedback> todosFeedbacks = feedbackRepository.findAll();
    
    // Buscar informações do usuário logado
    Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(usuarioLogin);
    Usuario usuario = usuarioOpt.orElse(null);
    
    String tipoUsuarioLogado = usuario != null ? usuario.getTipoUsuario().name() : "";
    
    System.out.println("🔍 Buscando feedbacks para: " + usuarioLogin + " (Tipo: " + tipoUsuarioLogado + ")");
    
    List<Feedback> feedbacksVisiveis = todosFeedbacks.stream()
        .filter(feedback -> {
            boolean isPublico = feedback.isPublico();
            boolean isProprioFeedback = usuarioLogin.equals(feedback.getAlunoId());
            
            // 🔥 CORREÇÃO: Verificar se é destinatário
            boolean isDestinatario = false;
            
            // 1. Verificar se é destinatário específico
            if (feedback.getDestinatarioEspecifico() != null && 
                !feedback.getDestinatarioEspecifico().isEmpty()) {
                isDestinatario = usuarioLogin.equals(feedback.getDestinatarioEspecifico());
                System.out.println("🎯 Verificando destinatário específico: " + feedback.getDestinatarioEspecifico() + " == " + usuarioLogin + " = " + isDestinatario);
            }
            
            // 2. Verificar se é do mesmo tipo/grupo (para feedbacks sem destinatário específico)
            if (!isDestinatario && usuario != null) {
                boolean isMesmoTipo = tipoUsuarioLogado.equals(feedback.getDestinatario().name());
                boolean temDestinatarioEspecifico = feedback.getDestinatarioEspecifico() != null && 
                                                   !feedback.getDestinatarioEspecifico().isEmpty();
                
                // Só é destinatário por tipo se NÃO tiver destinatário específico
                isDestinatario = isMesmoTipo && !temDestinatarioEspecifico;
                System.out.println("👥 Verificando mesmo tipo: " + tipoUsuarioLogado + " == " + feedback.getDestinatario() + " = " + isMesmoTipo);
            }
            
            boolean visivel = isPublico || isProprioFeedback || isDestinatario;
            
            if (visivel) {
                System.out.println("👁️  Feedback " + feedback.getId() + " VISÍVEL para " + usuarioLogin + 
                                 " | Público: " + isPublico + 
                                 " | Próprio: " + isProprioFeedback + 
                                 " | Destinatário: " + isDestinatario +
                                 " | Dest. Específico: " + feedback.getDestinatarioEspecifico());
            }
            
            return visivel;
        })
        .collect(Collectors.toList());
    
    System.out.println("✅ Feedbacks visíveis para " + usuarioLogin + ": " + feedbacksVisiveis.size());
    return feedbacksVisiveis;
}
    
    /**
     * 🔍 Listar feedbacks privados direcionados a um destinatário específico (recebidos)
     */
    public List<Feedback> listarPorDestinatarioEspecifico(String destinatarioLogin) {
        System.out.println("🔍 Buscando feedbacks privados recebidos por: " + destinatarioLogin);
        List<Feedback> feedbacks = feedbackRepository.findByDestinatarioEspecificoAndPublicoFalse(destinatarioLogin);
        System.out.println("✅ Feedbacks recebidos encontrados: " + feedbacks.size());
        return feedbacks;
    }
   

    /**
     * Salvar novo feedback
     */
    public Feedback salvar(Feedback feedback) {
        try {
            System.out.println("💾 Salvando feedback no banco: titulo='" + feedback.getTitulo() + "', alunoId='" + feedback.getAlunoId() + "', publico='" + feedback.isPublico() + "', destinatarioEspecifico='" + feedback.getDestinatarioEspecifico() + "'");
            Feedback saved = feedbackRepository.saveAndFlush(feedback);
            System.out.println("✅ Feedback salvo com id=" + (saved != null ? saved.getId() : "null"));
            return saved;
        } catch (Exception e) {
            System.out.println("❌ Exceção ao salvar feedback: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Buscar feedback por ID
     */
    public Feedback buscarPorId(Long id) {
        return feedbackRepository.findById(id).orElse(null);
    }

    /**
     * Deletar feedback
     */
    public void deletar(Long id) {
        feedbackRepository.deleteById(id);
    }
}
