package br.com.fatec.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro histórico de cada folha de ponto gerada (download do DOCX oficial).
 * Os dados do período letivo são copiados para preservar o histórico mesmo que a grade seja excluída.
 */
@Entity
@Table(name = "registro_folha_ponto")
public class RegistroFolhaPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_registro_folha_professor"))
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", foreignKey = @ForeignKey(name = "fk_registro_folha_grade"))
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_registro_folha_usuario"))
    private Usuario usuario;

    @Column(nullable = false)
    private Short mes;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "periodo_letivo_ano")
    private Integer periodoLetivoAno;

    @Column(name = "periodo_letivo_semestre")
    private Short periodoLetivoSemestre;

    @Column(name = "grade_outro_periodo", nullable = false)
    private boolean gradeOutroPeriodo;

    @Column(name = "dias_sem_aula", nullable = false)
    private Short diasSemAula;

    @Column(name = "quantidade_disciplinas", nullable = false)
    private Short quantidadeDisciplinas;

    @Column(nullable = false, length = 10)
    private String formato = "DOCX";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RegistroFolhaPonto() {
    }

    public RegistroFolhaPonto(Professor professor, Grade grade, Usuario usuario, int mes, int ano,
                              Integer periodoLetivoAno, Integer periodoLetivoSemestre,
                              boolean gradeOutroPeriodo, int diasSemAula, int quantidadeDisciplinas) {
        this.professor = professor;
        this.grade = grade;
        this.usuario = usuario;
        this.mes = (short) mes;
        this.ano = ano;
        this.periodoLetivoAno = periodoLetivoAno;
        this.periodoLetivoSemestre = periodoLetivoSemestre != null ? periodoLetivoSemestre.shortValue() : null;
        this.gradeOutroPeriodo = gradeOutroPeriodo;
        this.diasSemAula = (short) diasSemAula;
        this.quantidadeDisciplinas = (short) quantidadeDisciplinas;
    }

    public Long getId() { return id; }
    public Professor getProfessor() { return professor; }
    public Grade getGrade() { return grade; }
    public Usuario getUsuario() { return usuario; }
    public Short getMes() { return mes; }
    public Integer getAno() { return ano; }
    public Integer getPeriodoLetivoAno() { return periodoLetivoAno; }
    public Short getPeriodoLetivoSemestre() { return periodoLetivoSemestre; }
    public boolean isGradeOutroPeriodo() { return gradeOutroPeriodo; }
    public Short getDiasSemAula() { return diasSemAula; }
    public Short getQuantidadeDisciplinas() { return quantidadeDisciplinas; }
    public String getFormato() { return formato; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
