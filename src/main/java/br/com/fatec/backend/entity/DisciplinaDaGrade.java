package br.com.fatec.backend.entity;

import br.com.fatec.backend.enums.StatusDisciplinaGrade;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "disciplina_da_grade")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DisciplinaDaGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disciplina_grade_grade"))
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disciplina_grade_disciplina"))
    private Disciplina disciplina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disciplina_grade_curso"))
    private Curso curso;

    @Column(name = "semestre_curso")
    private Short semestreCurso;

    @Column(length = 10)
    private String periodo;

    @Column(name = "quantidade_aulas", nullable = false)
    private Short quantidadeAulas;

    private Integer op;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDisciplinaGrade status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected DisciplinaDaGrade() {}

    public DisciplinaDaGrade(Grade grade, Disciplina disciplina, Curso curso, Short semestreCurso, String periodo, Short quantidadeAulas, Integer op, StatusDisciplinaGrade status) {
        this.grade = grade;
        this.disciplina = disciplina;
        this.curso = curso;
        this.semestreCurso = semestreCurso;
        this.periodo = periodo;
        this.quantidadeAulas = quantidadeAulas;
        this.op = op;
        this.status = status;
    }

    public void atualizarDados(Grade grade, Disciplina disciplina, Curso curso, Short semestreCurso, String periodo, Short quantidadeAulas, Integer op, StatusDisciplinaGrade status) {
        this.grade = grade;
        this.disciplina = disciplina;
        this.curso = curso;
        this.semestreCurso = semestreCurso;
        this.periodo = periodo;
        this.quantidadeAulas = quantidadeAulas;
        this.op = op;
        this.status = status;
    }

    public Long getId() { return id; }
    public Grade getGrade() { return grade; }
    public Disciplina getDisciplina() { return disciplina; }
    public Curso getCurso() { return curso; }
    public Short getSemestreCurso() { return semestreCurso; }
    public String getPeriodo() { return periodo; }
    public Short getQuantidadeAulas() { return quantidadeAulas; }
    public Integer getOp() { return op; }
    public StatusDisciplinaGrade getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}