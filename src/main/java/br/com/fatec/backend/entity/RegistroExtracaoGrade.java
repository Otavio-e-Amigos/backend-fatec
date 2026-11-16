package br.com.fatec.backend.entity;

import br.com.fatec.backend.enums.StatusExtracaoGrade;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro histórico de uma extração de grade a partir de PDF.
 * Não guarda o CPF extraído nem o conteúdo do arquivo: apenas metadados do processamento.
 */
@Entity
@Table(name = "registro_extracao_grade")
public class RegistroExtracaoGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_registro_extracao_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", foreignKey = @ForeignKey(name = "fk_registro_extracao_professor"))
    private Professor professor;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusExtracaoGrade status;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    @Column(name = "professor_nome", length = 150)
    private String professorNome;

    @Column(name = "professor_matricula", length = 20)
    private String professorMatricula;

    private Short semestre;

    private Integer ano;

    @Column(name = "quantidade_disciplinas", nullable = false)
    private int quantidadeDisciplinas;

    @Column(name = "quantidade_aulas", nullable = false)
    private int quantidadeAulas;

    @Column(name = "quantidade_avisos", nullable = false)
    private int quantidadeAvisos;

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RegistroExtracaoGrade() {
    }

    public RegistroExtracaoGrade(Usuario usuario, String nomeArquivo, Long tamanhoBytes,
                                 StatusExtracaoGrade status, Long duracaoMs) {
        this.usuario = usuario;
        this.nomeArquivo = nomeArquivo;
        this.tamanhoBytes = tamanhoBytes;
        this.status = status;
        this.duracaoMs = duracaoMs;
    }

    public void preencherResultado(Professor professorCadastrado, String professorNome, String professorMatricula,
                                   Short semestre, Integer ano, int quantidadeDisciplinas,
                                   int quantidadeAulas, int quantidadeAvisos) {
        this.professor = professorCadastrado;
        this.professorNome = professorNome;
        this.professorMatricula = professorMatricula;
        this.semestre = semestre;
        this.ano = ano;
        this.quantidadeDisciplinas = quantidadeDisciplinas;
        this.quantidadeAulas = quantidadeAulas;
        this.quantidadeAvisos = quantidadeAvisos;
    }

    public void registrarErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Professor getProfessor() { return professor; }
    public String getNomeArquivo() { return nomeArquivo; }
    public Long getTamanhoBytes() { return tamanhoBytes; }
    public StatusExtracaoGrade getStatus() { return status; }
    public String getMensagemErro() { return mensagemErro; }
    public String getProfessorNome() { return professorNome; }
    public String getProfessorMatricula() { return professorMatricula; }
    public Short getSemestre() { return semestre; }
    public Integer getAno() { return ano; }
    public int getQuantidadeDisciplinas() { return quantidadeDisciplinas; }
    public int getQuantidadeAulas() { return quantidadeAulas; }
    public int getQuantidadeAvisos() { return quantidadeAvisos; }
    public Long getDuracaoMs() { return duracaoMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
