package br.com.fatec.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "aula_da_grade")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class AulaDaGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disciplina_grade_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aula_grade_disciplina_grade"))
    private DisciplinaDaGrade disciplinaDaGrade;

    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    protected AulaDaGrade() {}

    public AulaDaGrade(DisciplinaDaGrade disciplinaDaGrade, Short diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.disciplinaDaGrade = disciplinaDaGrade;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long getId() { return id; }
    public DisciplinaDaGrade getDisciplinaDaGrade() { return disciplinaDaGrade; }
    public Short getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
}
