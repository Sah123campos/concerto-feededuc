package com.projeto.feededuc.backend.dto;

import com.projeto.feededuc.backend.model.TipoUsuario;
import com.projeto.feededuc.backend.model.StatusFeedback; // Importação correta do enum
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para Feedback, usado para transferir dados entre 
 * o Controller e a Service/Requisições HTTP.
 */
public class FeedbackDTO {
    
    // Campos que podem ser enviados pelo cliente
    private String titulo;
    private String conteudo;
    private TipoUsuario destinatario;
    private boolean isPublico;

    // Campos que podem ser retornados (ReadOnly)
    private Long id;
    private String alunoId;
    private StatusFeedback status;
    private LocalDateTime dataEnvio;
    
    // Construtor vazio
    public FeedbackDTO() {}

    // Construtor para criação (POST)
    public FeedbackDTO(String titulo, String conteudo, TipoUsuario destinatario, boolean isPublico) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.destinatario = destinatario;
        this.isPublico = isPublico;
    }

    // Getters e Setters (para brevidade, omitidos, mas seriam necessários)
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public TipoUsuario getDestinatario() { return destinatario; }
    public void setDestinatario(TipoUsuario destinatario) { this.destinatario = destinatario; }

    public boolean isPublico() { return isPublico; }
    public void setPublico(boolean isPublico) { this.isPublico = isPublico; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlunoId() { return alunoId; }
    public void setAlunoId(String alunoId) { this.alunoId = alunoId; }

    public StatusFeedback getStatus() { return status; }
    public void setStatus(StatusFeedback status) { this.status = status; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
}