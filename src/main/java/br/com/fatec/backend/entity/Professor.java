package br.com.fatec.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "professor")
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 30)
    private String codigo;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, unique = true, length = 30)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_contrato", nullable = false, length = 20)
    private RegimeContrato regimeContrato;

    @Column(name = "regime_juridico", length = 20)
    private String regimeJuridico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusProfessor status;

    @Enumerated(EnumType.STRING)
    @Column(name = "titulacao", length = 30)
    private Titulacao titulacao;

    protected Professor() {

    }

    public Professor(String nome, String codigo, String cpf, String matricula, RegimeContrato regimeContrato, String regimeJuridico, StatusProfessor status, Titulacao titulacao) {
        this.nome = nome;
        this.codigo = codigo;
        this.cpf = cpf;
        this.matricula = matricula;
        this.regimeContrato = regimeContrato;
        this.regimeJuridico = regimeJuridico;
        this.status = status;
        this.titulacao = titulacao;
    }

    public void atualizarDados(String nome, String codigo, String cpf, String matricula, RegimeContrato regimeContrato, String regimeJuridico, Titulacao titulacao) {
        this.nome = nome;
        this.codigo = codigo;
        this.cpf = cpf;
        this.matricula = matricula;
        this.regimeContrato = regimeContrato;
        this.regimeJuridico = regimeJuridico;
        this.titulacao = titulacao;
    }
    public void ativar() {
        this.status = StatusProfessor.ATIVO;
    }

    public void desativar() {
        this.status = StatusProfessor.INATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCpf() {
        return cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public RegimeContrato getRegimeContrato() {
        return regimeContrato;
    }

    public String getRegimeJuridico() {
        return regimeJuridico;
    }

    public StatusProfessor getStatus() {
        return status;
    }

    public Titulacao getTitulacao() {
        return titulacao;
    }

    public void atualizarNome(String nome) {
        this.nome = nome;
    }

    public void atualizarCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void atualizarCpf(String cpf) {
        this.cpf = cpf;
    }

    public void atualizarMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void atualizarRegimeContrato(RegimeContrato regimeContrato) {
        this.regimeContrato = regimeContrato;
    }

    public void atualizarRegimeJuridico(String regimeJuridico) {
        this.regimeJuridico = regimeJuridico;
    }

    public void atualizarStatus(StatusProfessor status) {
        this.status = status;
    }

    public void atualizarTitulacao(Titulacao titulacao) {
        this.titulacao = titulacao;
    }
}