package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.disciplina_da_grade.DisciplinaDaGradeRequisicaoDTO;
import br.com.fatec.backend.dto.disciplina_da_grade.DisciplinaDaGradeRespostaDTO;
import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.CursoRepository;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import br.com.fatec.backend.repository.DisciplinaRepository;
import br.com.fatec.backend.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaDaGradeService {

    private final DisciplinaDaGradeRepository repository;
    private final GradeRepository gradeRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final CursoRepository cursoRepository;

    public DisciplinaDaGradeService(
            DisciplinaDaGradeRepository repository,
            GradeRepository gradeRepository,
            DisciplinaRepository disciplinaRepository,
            CursoRepository cursoRepository
    ) {
        this.repository = repository;
        this.gradeRepository = gradeRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.cursoRepository = cursoRepository;
    }

    private DisciplinaDaGrade buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Associação Disciplina da Grade não encontrada com ID: " + id));
    }

    private Grade buscarGradePorId(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grade não encontrada com ID: " + id));
    }

    private Disciplina buscarDisciplinaPorId(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina não encontrada com ID: " + id));
    }

    private Curso buscarCursoPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DisciplinaDaGradeRespostaDTO> listarPorGrade(Long gradeId) {
        if (!gradeRepository.existsById(gradeId)) {
            throw new RecursoNaoEncontradoException("Grade não encontrada com ID: " + gradeId);
        }

        return repository.findByGradeId(gradeId).stream()
                .map(DisciplinaDaGradeRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaDaGradeRespostaDTO buscarPorId(Long id) {
        return DisciplinaDaGradeRespostaDTO.daEntidade(buscarEntidadePorId(id));
    }

    @Transactional
    public DisciplinaDaGradeRespostaDTO criar(DisciplinaDaGradeRequisicaoDTO requisicao) {
        boolean jaExiste = repository.existsByGradeIdAndDisciplinaIdAndCursoId(
                requisicao.gradeId(),
                requisicao.disciplinaId(),
                requisicao.cursoId()
        );

        if (jaExiste) {
            throw new ConflitoException("Esta disciplina já está vinculada a este curso na grade informada.");
        }

        Grade grade = buscarGradePorId(requisicao.gradeId());
        Disciplina disciplina = buscarDisciplinaPorId(requisicao.disciplinaId());
        Curso curso = buscarCursoPorId(requisicao.cursoId());

        String periodoTratado = requisicao.periodo() != null ? requisicao.periodo().trim().toUpperCase() : null;

        DisciplinaDaGrade vinculo = new DisciplinaDaGrade(
                grade,
                disciplina,
                curso,
                requisicao.semestreCurso(),
                periodoTratado,
                requisicao.quantidadeAulas(),
                requisicao.op(),
                requisicao.status()
        );

        return DisciplinaDaGradeRespostaDTO.daEntidade(repository.save(vinculo));
    }

    @Transactional
    public DisciplinaDaGradeRespostaDTO atualizar(Long id, DisciplinaDaGradeRequisicaoDTO requisicao) {
        DisciplinaDaGrade vinculo = buscarEntidadePorId(id);

        Grade grade = buscarGradePorId(requisicao.gradeId());
        Disciplina disciplina = buscarDisciplinaPorId(requisicao.disciplinaId());
        Curso curso = buscarCursoPorId(requisicao.cursoId());

        String periodoTratado = requisicao.periodo() != null ? requisicao.periodo().trim().toUpperCase() : null;

        vinculo.atualizarDados(
                grade,
                disciplina,
                curso,
                requisicao.semestreCurso(),
                periodoTratado,
                requisicao.quantidadeAulas(),
                requisicao.op(),
                requisicao.status()
        );

        return DisciplinaDaGradeRespostaDTO.daEntidade(vinculo);
    }

    @Transactional
    public void excluir(Long id) {
        DisciplinaDaGrade vinculo = buscarEntidadePorId(id);

        repository.delete(vinculo);
    }
}