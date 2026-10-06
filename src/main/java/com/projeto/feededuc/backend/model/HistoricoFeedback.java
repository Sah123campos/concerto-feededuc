package com.projeto.feededuc.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime; // Import atualizado

@Entity
@Table(name = "historico_feedback")
public class HistoricoFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relacionamentos ---
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(optional = true)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario; // Quem realizou a ação no histórico (Professor, Admin, etc.)

    // --- Snapshot do Feedback no momento da Ação ---
    
    @Column(name = "setor_alvo", length = 100) // Nome atualizado
    private String setorAlvo;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao; // Tipo de dado atualizado

    @Column(columnDefinition = "TEXT")
    private String conteudo; // Nome atualizado (era 'mensagem')

    @Column(length = 10)
    private String privacidade; // publico | privado (Mantido como String para log de estado)

    @Column(length = 255)
    private String titulo;
    
    // --- Dados do Histórico ---

    @Column(length = 20, nullable = false)
    private String acao; // criado | atualizado | respondido | status_alterado

    @Column(name = "data_modificacao")
    private LocalDateTime dataModificacao = LocalDateTime.now(); // Tipo de dado atualizado
    
    // Construtor vazio
    public HistoricoFeedback() {}
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getSetorAlvo() { // Getters e Setters atualizados
        return setorAlvo;
    }

    public void setSetorAlvo(String setorAlvo) { // Getters e Setters atualizados
        this.setorAlvo = setorAlvo;
    }

    public LocalDateTime getDataCriacao() { // Tipo de dado atualizado
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) { // Tipo de dado atualizado
        this.dataCriacao = dataCriacao;
    }

    public String getConteudo() { // Getters e Setters atualizados
        return conteudo;
    }

    public void setConteudo(String conteudo) { // Getters e Setters atualizados
        this.conteudo = conteudo;
    }

    public String getPrivacidade() {
        return privacidade;
    }

    public void setPrivacidade(String privacidade) {
        this.privacidade = privacidade;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public LocalDateTime getDataModificacao() { // Tipo de dado atualizado
        return dataModificacao;
    }

    public void setDataModificacao(LocalDateTime dataModificacao) { // Tipo de dado atualizado
        this.dataModificacao = dataModificacao;
    }
}