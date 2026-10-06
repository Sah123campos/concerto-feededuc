package com.projeto.feededuc.backend.model;

/**
 * Representa os possíveis status de um Feedback dentro do sistema.
 */
public enum StatusFeedback {
    
    /** Recém-enviado e aguarda triagem ou designação. */
    EM_ABERTO,
    
    /** Visto por um responsável (gestor/professor) e a análise está em andamento. */
    EM_ANALISE,
    
    /** A questão levantada foi resolvida e o ciclo de vida foi concluído. */
    RESOLVIDO
}