package br.com.fatec.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Marco persistente indicando que o calendário acadêmico de um ano foi revisado/atualizado.
 * Usado pelo lembrete anual de fevereiro.
 */
@Entity
@Table(name = "calendario_atualizacao")
public class CalendarioAtualizacao {

    @Id
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_calendario_atualizacao_usuario"))
    private Usuario usuario;

    @Column(length = 500)
    private String observacao;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected CalendarioAtualizacao() {
    }

    public CalendarioAtualizacao(Integer ano, Usuario usuario, String observacao, LocalDateTime atualizadoEm) {
        this.ano = ano;
        this.usuario = usuario;
        this.observacao = observacao;
        this.atualizadoEm = atualizadoEm;
    }

    public void reconfirmar(Usuario usuario, String observacao, LocalDateTime atualizadoEm) {
        this.usuario = usuario;
        this.observacao = observacao;
        this.atualizadoEm = atualizadoEm;
    }

    public Integer getAno() { return ano; }
    public Usuario getUsuario() { return usuario; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
