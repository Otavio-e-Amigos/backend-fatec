package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRequisicaoDTO;
import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRespostaDTO;
import br.com.fatec.backend.entity.AulaDaGrade;
import br.com.fatec.backend.entity.DisciplinaDaGrade;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.AulaDaGradeRepository;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AulaDaGradeService {

    private final AulaDaGradeRepository aulaRepository;
    private final DisciplinaDaGradeRepository disciplinaDaGradeRepository;

    public AulaDaGradeService(AulaDaGradeRepository aulaRepository,
                              DisciplinaDaGradeRepository disciplinaDaGradeRepository) {
        this.aulaRepository = aulaRepository;
        this.disciplinaDaGradeRepository = disciplinaDaGradeRepository;
    }

    @Transactional(readOnly = true)
    public List<AulaDaGradeRespostaDTO> listarPorDisciplinaDaGrade(Long disciplinaGradeId) {
        if (!disciplinaDaGradeRepository.existsById(disciplinaGradeId)) {
            throw new RecursoNaoEncontradoException("Disciplina da grade não encontrada com ID: " + disciplinaGradeId);
        }

        return aulaRepository.findByDisciplinaDaGradeId(disciplinaGradeId).stream()
                .map(AulaDaGradeRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional
    public AulaDaGradeRespostaDTO registrarAula(Long disciplinaGradeId, AulaDaGradeRequisicaoDTO requisicao) {
        DisciplinaDaGrade disciplinaDaGrade = disciplinaDaGradeRepository.findById(disciplinaGradeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina da grade não encontrada com ID: " + disciplinaGradeId));

        if (!requisicao.horaFim().isAfter(requisicao.horaInicio())) {
            throw new IllegalArgumentException("A hora de término deve ser obrigatoriamente posterior à hora de início.");
        }

        Long gradeId = disciplinaDaGrade.getGrade().getId();
        List<AulaDaGrade> aulasExistentesNaGrade = aulaRepository.findByGradeIdComDetalhes(gradeId);

        for (AulaDaGrade aulaExistente : aulasExistentesNaGrade) {
            if (aulaExistente.getDiaSemana().equals(requisicao.diaSemana())) {

                boolean háSobreposição = requisicao.horaInicio().isBefore(aulaExistente.getHoraFim())
                        && aulaExistente.getHoraInicio().isBefore(requisicao.horaFim());

                if (háSobreposição) {
                    throw new ConflitoException("Conflito de horários: Já existe uma aula cadastrada nesta grade no dia e horário informados.");
                }
            }
        }

        AulaDaGrade novaAula = new AulaDaGrade(
                disciplinaDaGrade,
                requisicao.diaSemana(),
                requisicao.horaInicio(),
                requisicao.horaFim()
        );

        return AulaDaGradeRespostaDTO.daEntidade(aulaRepository.save(novaAula));
    }

    @Transactional
    public void excluir(Long id) {
        AulaDaGrade aula = aulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula da grade não encontrada com ID: " + id));

        aulaRepository.delete(aula);
    }
}