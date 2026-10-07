package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRequisicaoDTO;
import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRespostaDTO;
import br.com.fatec.backend.entity.PeriodoLetivo;
import br.com.fatec.backend.exception.ConflitoException; // Retorna 409
import br.com.fatec.backend.exception.RegraNegocioException; // Retorna 400
import br.com.fatec.backend.exception.RecursoNaoEncontradoException; // Retorna 404
import br.com.fatec.backend.repository.PeriodoLetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PeriodoLetivoService {

    private final PeriodoLetivoRepository repository;

    public PeriodoLetivoService(PeriodoLetivoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PeriodoLetivoRespostaDTO criar(PeriodoLetivoRequisicaoDTO requisicao) {
        if (repository.existsByAnoAndSemestre(requisicao.ano(), requisicao.semestre())) {
            throw new ConflitoException(
                    "Período letivo " + requisicao.ano() + "/" + requisicao.semestre() + " já cadastrado."
            );
        }

        if (!requisicao.dataInicio().isBefore(requisicao.dataFim())) {
            throw new RegraNegocioException("A data inicial deve ser anterior à data final.");
        }

        PeriodoLetivo periodo = new PeriodoLetivo(
                requisicao.semestre(),
                requisicao.ano(),
                requisicao.dataInicio(),
                requisicao.dataFim()
        );

        return PeriodoLetivoRespostaDTO.daEntidade(repository.save(periodo));
    }

    @Transactional(readOnly = true)
    public List<PeriodoLetivoRespostaDTO> listarTodos() {
        return repository.findAllByOrderByAnoDescSemestreDesc().stream()
                .map(PeriodoLetivoRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public PeriodoLetivoRespostaDTO buscarPorId(Long id) {
        PeriodoLetivo periodo = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Período letivo não encontrado com o ID: " + id));
        return PeriodoLetivoRespostaDTO.daEntidade(periodo);
    }
}