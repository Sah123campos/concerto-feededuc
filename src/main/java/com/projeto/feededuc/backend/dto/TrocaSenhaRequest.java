package com.projeto.feededuc.backend.dto;

public record TrocaSenhaRequest(
    String senhaAtual,
    String novaSenha
) {}