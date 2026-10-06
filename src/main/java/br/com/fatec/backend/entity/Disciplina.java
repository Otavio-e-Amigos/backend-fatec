package br.com.fatec.backend.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "disciplina")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 8)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String nome;

    protected Disciplina() {}

    public Disciplina(String codigo, String sigla, String nome) {
        this.codigo = codigo;
        this.sigla = sigla;
        this.nome = nome;

    }

    public void atualizarDados(String codigo, String sigla, String nome){
        this.codigo = codigo;
        this.sigla = sigla;
        this.nome = nome;
    }

    public Long getId() {return id;}
    public String getCodigo() {return codigo;}
    public String getSigla() {return sigla;}
    public String getNome() {return nome;}

}
