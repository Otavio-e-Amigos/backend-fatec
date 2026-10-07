package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.curso.CursoRequisicaoDTO;
import br.com.fatec.backend.dto.curso.CursoRespostaDTO;
import br.com.fatec.backend.entity.Curso;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository repository;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CursoRespostaDTO> listarTodos() {
        return repository.findAllByOrderByNomeAsc().stream()
                .map(CursoRespostaDTO::daEntidadeCompleta)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoRespostaDTO buscarPorId(Long id) {
        return CursoRespostaDTO.daEntidadeCompleta(buscarEntidadePorId(id));
    }

    @Transactional
    public CursoRespostaDTO criar(CursoRequisicaoDTO requisicao) {
        String nome = requisicao.nome().trim();
        String sigla = requisicao.sigla().trim();

        validarUnicidadePorTurno(nome, sigla, requisicao.turno(), null);

        Curso curso = new Curso(nome, requisicao.turno(), sigla);

        return CursoRespostaDTO.daEntidadeCompleta(repository.save(curso));
    }

    @Transactional
    public CursoRespostaDTO atualizar(Long id, CursoRequisicaoDTO requisicao) {
        Curso curso = buscarEntidadePorId(id);

        String nome = requisicao.nome().trim();
        String sigla = requisicao.sigla().trim();

        validarUnicidadePorTurno(nome, sigla, requisicao.turno(), id);

        curso.atualizarDados(nome, requisicao.turno(), sigla);

        return CursoRespostaDTO.daEntidadeCompleta(curso);
    }

    private Curso buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Curso não encontrado com ID: " + id));
    }

    private void validarUnicidadePorTurno(String nome, String sigla, br.com.fatec.backend.entity.Turno turno, Long idAtual) {
        boolean nomeEmUso = (idAtual == null)
                ? repository.existsByUnidadeAndNomeIgnoreCaseAndTurno(Curso.UNIDADE_PADRAO, nome, turno)
                : repository.existsByUnidadeAndNomeIgnoreCaseAndTurnoAndIdNot(Curso.UNIDADE_PADRAO, nome, turno, idAtual);

        if (nomeEmUso) {
            throw new ConflitoException("Já existe um curso com este nome no turno da " + turno + ".");
        }

        boolean siglaEmUso = (idAtual == null)
                ? repository.existsByUnidadeAndSiglaIgnoreCaseAndTurno(Curso.UNIDADE_PADRAO, sigla, turno)
                : repository.existsByUnidadeAndSiglaIgnoreCaseAndTurnoAndIdNot(Curso.UNIDADE_PADRAO, sigla, turno, idAtual);

        if (siglaEmUso) {
            throw new ConflitoException("Já existe um curso com esta sigla no turno da " + turno + ".");
        }
    }
}