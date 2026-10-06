package com.projeto.feededuc.backend.dto;

// NOVO: DTO simples para criação de Feedback
// Ex: com.projeto.feededuc.backend.dto.NovoFeedbackRequest.java
public record NovoFeedbackRequest(
    String titulo,
    String conteudo,
    String setorAlvo,    // Categoria do destinatário
    Integer nota,        // 1 a 5 (se aplicável)
    boolean privado      // Se é privado ou público
) {}