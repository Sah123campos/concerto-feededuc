package com.projeto.feededuc.backend.model;     

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String conteudo;

    private String responsavelId;      // Login de quem respondeu
    private String responsavelNome;    // Nome de quem respondeu
    private String responsavelTipo;    // Tipo de usuário que respondeu

    private LocalDateTime dataResposta = LocalDateTime.now();

    // 🔥 NOVO: Status após resposta (opcional)
    @Enumerated(EnumType.STRING)
    private StatusFeedback novoStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    @JsonBackReference
    private Feedback feedback;

    public Resposta(String conteudo, String responsavelId, String responsavelNome, String responsavelTipo) {
        this.conteudo = conteudo;
        this.responsavelId = responsavelId;
        this.responsavelNome = responsavelNome;
        this.responsavelTipo = responsavelTipo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getResponsavelId() { return responsavelId; }
    public void setResponsavelId(String responsavelId) { this.responsavelId = responsavelId; }

    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }

    public String getResponsavelTipo() { return responsavelTipo; }
    public void setResponsavelTipo(String responsavelTipo) { this.responsavelTipo = responsavelTipo; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public StatusFeedback getNovoStatus() { return novoStatus; }
    public void setNovoStatus(StatusFeedback novoStatus) { this.novoStatus = novoStatus; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }

}