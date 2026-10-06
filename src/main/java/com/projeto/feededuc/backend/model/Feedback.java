package com.projeto.feededuc.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Feedback {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alunoId;
    private String titulo;
    private String conteudo;
    
    @Enumerated(EnumType.STRING)
    private TipoUsuario destinatario;
    
    // 🔥 ADICIONE ESTE CAMPO
    private String destinatarioEspecifico; // Login do usuário específico
    
    private boolean publico = false;
    
    @Enumerated(EnumType.STRING)
    private StatusFeedback status = StatusFeedback.EM_ABERTO;

    private LocalDateTime dataEnvio = LocalDateTime.now();

    // Respostas (thread) — múltiplas respostas por feedback
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "feedback", orphanRemoval = true)
    @JsonManagedReference
    private List<Resposta> respostas = new ArrayList<>();

    // Construtores
    public Feedback() {}

    public Feedback(String titulo, String conteudo, TipoUsuario destinatario, boolean publico) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.destinatario = destinatario;
        this.publico = publico;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAlunoId() { return alunoId; }
    public void setAlunoId(String alunoId) { this.alunoId = alunoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public TipoUsuario getDestinatario() { return destinatario; }
    public void setDestinatario(TipoUsuario destinatario) { this.destinatario = destinatario; }

    // 🔥 ADICIONE ESTES GETTERS/SETTERS
    public String getDestinatarioEspecifico() { return destinatarioEspecifico; }
    public void setDestinatarioEspecifico(String destinatarioEspecifico) { 
        this.destinatarioEspecifico = destinatarioEspecifico; 
    }

    public boolean isPublico() { return publico; }
    public void setPublico(boolean publico) { this.publico = publico; }

    public StatusFeedback getStatus() { return status; }
    public void setStatus(StatusFeedback status) { this.status = status; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public List<Resposta> getRespostas() { return respostas; }
    public void setRespostas(List<Resposta> respostas) { this.respostas = respostas; }

    public void addResposta(Resposta resposta) {
        respostas.add(resposta);
        resposta.setFeedback(this);
    }

    public void removeResposta(Resposta resposta) {
        respostas.remove(resposta);
        resposta.setFeedback(null);
    }

}
