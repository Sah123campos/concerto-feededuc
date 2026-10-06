package com.projeto.feededuc.backend.repository;

import com.projeto.feededuc.backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 🔍 Buscar feedbacks por alunoId
    List<Feedback> findByAlunoId(String alunoId);
    
    // 🔍 Buscar feedbacks públicos (não privados)
    List<Feedback> findByPublicoTrue();
    
    // 🔍 Buscar feedbacks por status
    List<Feedback> findByStatus(String status);
    
    // 🔍 Buscar feedbacks por destinatário
    List<Feedback> findByDestinatario(String destinatario);
    
    // 🔍 Buscar feedbacks privados direcionados a um destinatário específico
    List<Feedback> findByDestinatarioEspecificoAndPublicoFalse(String destinatarioEspecifico);
    
    // 🔍 Buscar feedbacks públicos OU do usuário específico
    @Query("SELECT f FROM Feedback f WHERE f.publico = false OR f.alunoId = :alunoId")
    List<Feedback> findFeedbacksVisiveisParaUsuario(@Param("alunoId") String alunoId);
}
