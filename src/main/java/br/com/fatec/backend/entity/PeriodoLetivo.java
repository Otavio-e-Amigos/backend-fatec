package br.com.fatec.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodo_letivo")
public class PeriodoLetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer semestre;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    protected PeriodoLetivo() {
    }

    public PeriodoLetivo(Integer semestre, Integer ano, LocalDate dataInicio, LocalDate dataFim) {
        this.semestre = semestre;
        this.ano = ano;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public Long getId() { return id; }
    public Integer getSemestre() { return semestre; }
    public Integer getAno() { return ano; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
}