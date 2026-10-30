package br.com.fatec.backend.service.extracao;

import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;

/** Contrato de extração. O Gemini é só uma implementação (permite trocar de provedor ou mockar). */
public interface ExtratorDeGrade {
    GradeExtraidaDTO extrair(byte[] pdf);
}