package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.professor.ProfessorRequisicaoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRespostaDTO;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.StatusProfessor;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.specification.ProfessorSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProfessorRespostaDTO> listarTodos() {
        return repository.findAll().stream()
                .map(ProfessorRespostaDTO::daEntidadeComCpfMascarado)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorRespostaDTO buscarPorId(Long id) {
        return ProfessorRespostaDTO.daEntidadeCompleta(buscarEntidadePorId(id));
    }

    @Transactional
    public ProfessorRespostaDTO criar(ProfessorRequisicaoDTO requisicao) {
        String cpf = normalizarCpf(requisicao.cpf());
        String matricula = requisicao.matricula().trim();

        validarUnicidade(cpf, matricula, null);

        Professor professor = new Professor(
                requisicao.nome().trim(),
                normalizarOpcional(requisicao.codigo()),
                cpf,
                matricula,
                requisicao.regimeContrato(),
                requisicao.regimeJuridico(),
                requisicao.titulacao()
        );

        return ProfessorRespostaDTO.daEntidadeCompleta(repository.save(professor));
    }

    @Transactional
    public ProfessorRespostaDTO atualizar(Long id, ProfessorRequisicaoDTO requisicao) {
        Professor professor = buscarEntidadePorId(id);

        String cpf = normalizarCpf(requisicao.cpf());
        String matricula = requisicao.matricula().trim();

        validarUnicidade(cpf, matricula, id);

        professor.atualizarDados(
                requisicao.nome().trim(),
                normalizarOpcional(requisicao.codigo()),
                cpf,
                matricula,
                requisicao.regimeContrato(),
                requisicao.regimeJuridico(),
                requisicao.titulacao()
        );

        return ProfessorRespostaDTO.daEntidadeCompleta(professor);
    }

    @Transactional(readOnly = true)
    public Page<ProfessorRespostaDTO> listar(String busca, Pageable pageable) {
        return repository.findAll(ProfessorSpecs.buscarPorNomeOuMatricula(busca), pageable)
                .map(ProfessorRespostaDTO::daEntidadeComCpfMascarado);
    }

    @Transactional
    public void alterarStatus(Long id, StatusProfessor status) {
        buscarEntidadePorId(id).alterarStatus(status);
    }

    private Professor buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Professor não encontrado com ID: " + id));
    }

    private void validarUnicidade(String cpf, String matricula, Long idAtual) {
        boolean cpfEmUso = repository.findByCpf(cpf)
                .filter(outro -> !outro.getId().equals(idAtual))
                .isPresent();
        if (cpfEmUso) {
            throw new ConflitoException("CPF já cadastrado.");
        }

        boolean matriculaEmUso = repository.findByMatricula(matricula)
                .filter(outro -> !outro.getId().equals(idAtual))
                .isPresent();
        if (matriculaEmUso) {
            throw new ConflitoException("Matrícula já cadastrada.");
        }
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private String normalizarOpcional(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}