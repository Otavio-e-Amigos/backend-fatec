package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.disciplina.DisciplinaRequisicaoDTO;
import br.com.fatec.backend.dto.disciplina.DisciplinaRespostaDTO;
import br.com.fatec.backend.dto.professor.ProfessorRequisicaoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRespostaDTO;
import br.com.fatec.backend.entity.Disciplina;
import br.com.fatec.backend.entity.Professor;
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

    // Repository usado para acessar as disciplinas no banco.
    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository) {this.repository = disciplinaRepository;}

    private Disciplina buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina não encontrada com ID: " + id));
    }

    //Listar todas as disciplinas
    @Transactional(readOnly = true)
    public List<DisciplinaRespostaDTO> listarTodos() {
        return repository.findAll().stream()
                .map(DisciplinaRespostaDTO::daEntidade)
                .toList();
    }

    //Buscar por ID
    @Transactional(readOnly = true)
    public DisciplinaRespostaDTO buscarPorId(Long id) {
        return DisciplinaRespostaDTO.daEntidade(buscarEntidadePorId(id));

    }

    //Busca por nome e listar
    @Transactional(readOnly = true)
    public Page<DisciplinaRespostaDTO> listar(String busca, Pageable pageable) {
        return repository.findAll(DisciplinaSpecs.buscarDisciplinaPorNome(busca), pageable)
                .map(DisciplinaRespostaDTO::daEntidade);
    }

    //Cadastrar
    @Transactional
    public DisciplinaRespostaDTO criar(DisciplinaRequisicaoDTO requisicao) {

        Disciplina disciplina = new Disciplina(
                requisicao.codigo(),
                requisicao.sigla(),
                requisicao.nome().trim()

        );

        return DisciplinaRespostaDTO.daEntidade(repository.save(disciplina));
    }

    //Atualizar
    @Transactional
    public DisciplinaRespostaDTO atualizar(Long id, DisciplinaRequisicaoDTO requisicao) {
        Disciplina disciplina = buscarEntidadePorId(id);


        disciplina.atualizarDados(
                requisicao.codigo(),
                requisicao.sigla(),
                requisicao.nome().trim()
        );

        return DisciplinaRespostaDTO.daEntidade(disciplina);
    }

}
