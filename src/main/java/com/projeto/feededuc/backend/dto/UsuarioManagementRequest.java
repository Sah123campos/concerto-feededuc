package com.projeto.feededuc.backend.dto;

import com.projeto.feededuc.backend.model.TipoUsuario;


/**
 * DTO usado pelo Admin para criar ou atualizar dados básicos de um usuário.
 */
public record UsuarioManagementRequest(
    String login, // CPF, Matrícula ou Email
    TipoUsuario usuario,    // ALUNO, PROFESSOR, GESTOR, ADMIN
    
    // O Admin não envia a senha, o sistema gera uma temporária
    String nome, // Exemplo de campo adicional
    String sobrenome // Exemplo de campo adicional
) {}

