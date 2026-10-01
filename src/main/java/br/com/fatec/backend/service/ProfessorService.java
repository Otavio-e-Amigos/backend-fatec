package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.professor.ProfessorRequisicaoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRespostaDTO;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.StatusProfessor;
import br.com.fatec.backend.exception.ConflitoException;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    private Professor buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Professor não encontrado com ID: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<ProfessorRespostaDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(ProfessorRespostaDTO::daEntidade)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorRespostaDTO buscarPorId(Long id) {
        return ProfessorRespostaDTO.daEntidade(
                buscarEntidadePorId(id)
        );
    }

    @Transactional
    public ProfessorRespostaDTO criar(ProfessorRequisicaoDTO requisicao) {

        String cpf = normalizarCpf(requisicao.cpf());

        validarCpf(cpf);

        if (repository.existsByCpf(cpf)) {
            throw new ConflitoException("CPF já cadastrado.");
        }

        if (repository.existsByMatricula(requisicao.matricula())) {
            throw new ConflitoException("Matrícula já cadastrada.");
        }

        StatusProfessor status = requisicao.status() != null
                ? requisicao.status()
                : StatusProfessor.ATIVO;

        Professor professor = new Professor(
                requisicao.nome(),
                requisicao.codigo(),
                cpf,
                requisicao.matricula(),
                requisicao.regimeContrato(),
                requisicao.regimeJuridico(),
                status,
                requisicao.titulacao()
        );

        Professor salvo = repository.save(professor);

        return ProfessorRespostaDTO.daEntidade(salvo);
    }

    @Transactional
    public ProfessorRespostaDTO atualizar(
            Long id,
            ProfessorRequisicaoDTO requisicao) {

        Professor professor = buscarEntidadePorId(id);

        String cpf = normalizarCpf(requisicao.cpf());

        validarCpf(cpf);

        if (repository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoException("CPF já cadastrado.");
        }

        if (repository.existsByMatriculaAndIdNot(
                requisicao.matricula(), id)) {

            throw new ConflitoException("Matrícula já cadastrada.");
        }

        professor.atualizarDados(
                requisicao.nome(),
                requisicao.codigo(),
                cpf,
                requisicao.matricula(),
                requisicao.regimeContrato(),
                requisicao.regimeJuridico(),
                requisicao.titulacao()
        );

        Professor atualizado = repository.save(professor);

        return ProfessorRespostaDTO.daEntidade(atualizado);
    }

    @Transactional
    public void ativar(Long id) {
        Professor professor = buscarEntidadePorId(id);
        professor.ativar();
        repository.save(professor);
    }

    @Transactional
    public void desativar(Long id) {
        Professor professor = buscarEntidadePorId(id);
        professor.desativar();
        repository.save(professor);
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    /*private void validarCpf(String cpf) {

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            throw new RegraNegocioException("CPF inválido.");
        }

        int primeiroDigito = calcularDigitoCpf(cpf, 9);
        int segundoDigito = calcularDigitoCpf(cpf, 10);

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))
                || segundoDigito != Character.getNumericValue(cpf.charAt(10))) {
            throw new RegraNegocioException("CPF inválido.");
        }
    }

    private int calcularDigitoCpf(String cpf, int quantidadeDigitos) {

        int soma = 0;
        int peso = quantidadeDigitos + 1;

        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (peso - i);
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }*/
    private void validarCpf(String cpf) {
        if (!cpf.matches("\\d{11}")) {
            throw new RegraNegocioException("CPF inválido.");
        }
    }
}