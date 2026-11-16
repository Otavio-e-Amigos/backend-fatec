package br.com.fatec.backend.entity;

import br.com.fatec.backend.enums.CategoriaNotificacao;
import br.com.fatec.backend.enums.TipoNotificacao;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notificação interna exibida no Appointer.
 * Cada registro pertence a um único usuário destinatário (controle individual de leitura).
 */
@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notificacao_usuario"))
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaNotificacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @Column(nullable = false)
    private boolean pendente = false;

    @Column(name = "resolvida_em")
    private LocalDateTime resolvidaEm;

    @Column(name = "chave_deduplicacao", length = 120)
    private String chaveDeduplicacao;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Notificacao() {
    }

    public Notificacao(Usuario usuario, TipoNotificacao tipo, String titulo, String mensagem) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.categoria = tipo.getCategoria();
        this.titulo = titulo;
        this.mensagem = mensagem;
    }

    public void definirReferencia(String referenciaTipo, Long referenciaId) {
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
    }

    public void definirPendencia(String chaveDeduplicacao, LocalDateTime expiraEm) {
        this.pendente = true;
        this.chaveDeduplicacao = chaveDeduplicacao;
        this.expiraEm = expiraEm;
    }

    public void definirChaveDeduplicacao(String chaveDeduplicacao) {
        this.chaveDeduplicacao = chaveDeduplicacao;
    }

    public void marcarComoLida(LocalDateTime momento) {
        if (!this.lida) {
            this.lida = true;
            this.lidaEm = momento;
        }
    }

    public void marcarComoNaoLida() {
        this.lida = false;
        this.lidaEm = null;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public CategoriaNotificacao getCategoria() { return categoria; }
    public TipoNotificacao getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public Long getReferenciaId() { return referenciaId; }
    public boolean isLida() { return lida; }
    public LocalDateTime getLidaEm() { return lidaEm; }
    public boolean isPendente() { return pendente; }
    public LocalDateTime getResolvidaEm() { return resolvidaEm; }
    public String getChaveDeduplicacao() { return chaveDeduplicacao; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
