package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.disciplina.DisciplinaRequisicaoDTO;
import br.com.fatec.backend.dto.disciplina.DisciplinaRespostaDTO;
import br.com.fatec.backend.entity.Disciplina;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.DisciplinaRepository;
import br.com.fatec.backend.specification.DisciplinaSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<DisciplinaRespostaDTO> listar(String busca, Pageable pageable) {
        return repository.findAll(DisciplinaSpecs.buscarPorTermo(busca), pageable)
                .map(DisciplinaRespostaDTO::daEntidade);
    }

    @Transactional(readOnly = true)
    public List<DisciplinaRespostaDTO> listarTodos() {
        return repository.findAllByOrderByNomeAsc().stream()
                .map(DisciplinaRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaRespostaDTO buscarPorId(Long id) {
        return DisciplinaRespostaDTO.daEntidade(buscarEntidadePorId(id));
    }

    @Transactional
    public DisciplinaRespostaDTO criar(DisciplinaRequisicaoDTO requisicao) {
        String codigo = requisicao.codigo().trim();
        String sigla = requisicao.sigla().trim();
        String nome = requisicao.nome().trim();

        validarRegrasDeNegocio(codigo, sigla, null);

        Disciplina disciplina = new Disciplina(codigo, sigla, nome);
        return DisciplinaRespostaDTO.daEntidade(repository.save(disciplina));
    }

    @Transactional
    public DisciplinaRespostaDTO atualizar(Long id, DisciplinaRequisicaoDTO requisicao) {
        Disciplina disciplina = buscarEntidadePorId(id);

        String codigo = requisicao.codigo().trim();
        String sigla = requisicao.sigla().trim();
        String nome = requisicao.nome().trim();

        validarRegrasDeNegocio(codigo, sigla, id);

        disciplina.atualizarDados(codigo, sigla, nome);
        return DisciplinaRespostaDTO.daEntidade(disciplina);
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina disciplina = buscarEntidadePorId(id);
        repository.delete(disciplina);
    }

    private Disciplina buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina não encontrada com ID: " + id));
    }

    private void validarRegrasDeNegocio(String codigo, String sigla, Long idAtual) {
        if (codigo.equalsIgnoreCase(sigla)) {
            throw new RegraNegocioException("O código e a sigla não podem ser iguais.");
        }

        boolean codigoEmUso = (idAtual == null)
                ? repository.existsByCodigoIgnoreCase(codigo)
                : repository.existsByCodigoIgnoreCaseAndIdNot(codigo, idAtual);

        if (codigoEmUso) {
            throw new ConflitoException("Já existe uma disciplina cadastrada com o código '" + codigo + "'.");
        }
    }
}