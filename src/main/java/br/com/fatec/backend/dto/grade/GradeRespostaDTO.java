package br.com.fatec.backend.dto.grade;

import br.com.fatec.backend.entity.Grade;

import java.math.BigDecimal;

public record GradeRespostaDTO(
        Long id,
        Long professorId,
        Long periodoLetivoId,
        BigDecimal semanal,
        BigDecimal mensal,
        BigDecimal total
) {
    public static GradeRespostaDTO daEntidade(Grade grade) {
        return new GradeRespostaDTO(
                grade.getId(),
                grade.getProfessor() != null ? grade.getProfessor().getId() : null,
                grade.getPeriodoLetivo() != null ? grade.getPeriodoLetivo().getId() : null,
                grade.getSemanal(),
                grade.getMensal(),
                grade.getTotal()
        );
    }
}