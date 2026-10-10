package br.com.fatec.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade")
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_professor"))
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_letivo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grade_periodo"))
    private PeriodoLetivo periodoLetivo;

    @Column(precision = 6, scale = 2)
    private BigDecimal semanal;

    @Column(precision = 6, scale = 2)
    private BigDecimal mensal;

    @Builder.Default
    @Column(precision = 6, scale = 2)
    private BigDecimal total;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Grade() {}

    public Grade(Professor professor, PeriodoLetivo periodoLetivo, BigDecimal semanal, BigDecimal mensal, BigDecimal total) {
        this.professor = professor;
        this.periodoLetivo = periodoLetivo;
        this.semanal = semanal;
        this.mensal = mensal;
        this.total = total;
    }

    public void atualizarDados(Professor professor, PeriodoLetivo periodoLetivo, BigDecimal semanal, BigDecimal mensal, BigDecimal total) {
        this.professor = professor;
        this.periodoLetivo = periodoLetivo;
        this.semanal = semanal;
        this.mensal = mensal;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public PeriodoLetivo getPeriodoLetivo() {
        return periodoLetivo;
    }

    public BigDecimal getSemanal() {
        return semanal;
    }

    public BigDecimal getMensal() {
        return mensal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}