package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.curso.CursoRequisicaoDTO;
import br.com.fatec.backend.dto.curso.CursoRespostaDTO;
import br.com.fatec.backend.entity.Curso;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.CursoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return repository.findAll(Sort.by("nome")).stream()
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

        validarNomeUnico(nome, null);
        validarSiglaUnica(sigla, null);

        Curso curso = new Curso(nome, requisicao.turno(), sigla);

        return CursoRespostaDTO.daEntidadeCompleta(repository.save(curso));
    }

    @Transactional
    public CursoRespostaDTO atualizar(Long id, CursoRequisicaoDTO requisicao) {
        Curso curso = buscarEntidadePorId(id);

        String nome = requisicao.nome().trim();
        String sigla = requisicao.sigla().trim();

        validarNomeUnico(nome, id);
        validarSiglaUnica(sigla, id);

        curso.atualizarDados(nome, requisicao.turno(), sigla);

        return CursoRespostaDTO.daEntidadeCompleta(curso);
    }

    @Transactional(readOnly = true)
    public Page<CursoRespostaDTO> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(CursoRespostaDTO::daEntidadeCompleta);
    }

    private Curso buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Curso não encontrado com ID: " + id));
    }

    private void validarNomeUnico(String nome, Long idAtual) {
        boolean nomeEmUso = (idAtual == null)
                ? repository.existsByUnidadeAndNomeIgnoreCase(Curso.UNIDADE_PADRAO, nome)
                : repository.existsByUnidadeAndNomeIgnoreCaseAndIdNot(Curso.UNIDADE_PADRAO, nome, idAtual);

        if (nomeEmUso) {
            throw new ConflitoException("Já existe um curso com este nome nesta unidade.");
        }
    }

    private void validarSiglaUnica(String sigla, Long idAtual) {
        boolean siglaEmUso = (idAtual == null)
                ? repository.existsByUnidadeAndSiglaIgnoreCase(Curso.UNIDADE_PADRAO, sigla)
                : repository.existsByUnidadeAndSiglaIgnoreCaseAndIdNot(Curso.UNIDADE_PADRAO, sigla, idAtual);

        if (siglaEmUso) {
            throw new ConflitoException("Já existe um curso com esta sigla nesta unidade.");
        }
    }
}