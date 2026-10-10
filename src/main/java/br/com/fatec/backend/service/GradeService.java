package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.grade.GradeRequisicaoDTO;
import br.com.fatec.backend.dto.grade.GradeRespostaDTO;
import br.com.fatec.backend.entity.Grade;
import br.com.fatec.backend.entity.PeriodoLetivo;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.PeriodoLetivoRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GradeService {

    private final GradeRepository repository;
    private final ProfessorRepository professorRepository;
    private final PeriodoLetivoRepository periodoLetivoRepository;

    public GradeService(GradeRepository repository,
                        ProfessorRepository professorRepository,
                        PeriodoLetivoRepository periodoLetivoRepository) {
        this.repository = repository;
        this.professorRepository = professorRepository;
        this.periodoLetivoRepository = periodoLetivoRepository;
    }

    @Transactional(readOnly = true)
    public Page<GradeRespostaDTO> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(GradeRespostaDTO::daEntidade);
    }

    @Transactional(readOnly = true)
    public List<GradeRespostaDTO> listarPorProfessor(Long professorId) {
        if (!professorRepository.existsById(professorId)) {
            throw new RecursoNaoEncontradoException("Professor não encontrado com ID: " + professorId);
        }

        return repository.findByProfessorId(professorId).stream()
                .map(GradeRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public GradeRespostaDTO buscarPorId(Long id) {
        return GradeRespostaDTO.daEntidade(buscarEntidadePorId(id));
    }

    @Transactional
    public GradeRespostaDTO criar(GradeRequisicaoDTO requisicao) {
        validarUnicidadeGrade(requisicao.professorId(), requisicao.periodoLetivoId(), null);

        Professor professor = buscarProfessorPorId(requisicao.professorId());
        PeriodoLetivo periodoLetivo = buscarPeriodoPorId(requisicao.periodoLetivoId());

        Grade grade = new Grade(
                professor,
                periodoLetivo,
                requisicao.semanal(),
                requisicao.mensal(),
                requisicao.total()
        );

        return GradeRespostaDTO.daEntidade(repository.save(grade));
    }

    @Transactional
    public GradeRespostaDTO atualizar(Long id, GradeRequisicaoDTO requisicao) {
        Grade grade = buscarEntidadePorId(id);

        validarUnicidadeGrade(requisicao.professorId(), requisicao.periodoLetivoId(), id);

        Professor professor = buscarProfessorPorId(requisicao.professorId());
        PeriodoLetivo periodoLetivo = buscarPeriodoPorId(requisicao.periodoLetivoId());

        grade.atualizarDados(professor, periodoLetivo, requisicao.semanal(), requisicao.mensal(), requisicao.total());

        return GradeRespostaDTO.daEntidade(grade);
    }


    private void validarUnicidadeGrade(Long professorId, Long periodoLetivoId, Long idAtual) {
        boolean jaExiste;

        if (idAtual == null) {
            jaExiste = repository.existsByProfessorIdAndPeriodoLetivoId(professorId, periodoLetivoId);
        } else {
            jaExiste = repository.existsByProfessorIdAndPeriodoLetivoIdAndIdNot(professorId, periodoLetivoId, idAtual);
        }

        if (jaExiste) {
            throw new ConflitoException(
                    "Já existe uma grade cadastrada para este professor no período letivo informado."
            );
        }
    }

    private Grade buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Grade não encontrada com ID: " + id));
    }

    private Professor buscarProfessorPorId(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado com ID: " + id));
    }

    private PeriodoLetivo buscarPeriodoPorId(Long id) {
        return periodoLetivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Período letivo não encontrado com ID: " + id));
    }
}