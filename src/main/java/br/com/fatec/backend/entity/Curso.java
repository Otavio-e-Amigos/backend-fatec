package br.com.fatec.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "curso")
public class Curso {

    public static final String UNIDADE_PADRAO = "FATEC Zona Leste";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Turno turno;

    @Column(nullable = false, length = 50, updatable = false)
    private String unidade = UNIDADE_PADRAO;

    @Column(nullable = false, length = 3)
    private String sigla;

    protected Curso() {}

    public Curso(String nome, Turno turno, String sigla) {
        this.nome = nome;
        this.turno = turno;
        this.sigla = sigla;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Turno getTurno() { return turno; }
    public String getUnidade() { return unidade; }
    public String getSigla() { return sigla; }

    public void atualizarDados(String nome, Turno turno, String sigla) {
        this.nome = nome;
        this.turno = turno;
        this.sigla = sigla;
    }
}