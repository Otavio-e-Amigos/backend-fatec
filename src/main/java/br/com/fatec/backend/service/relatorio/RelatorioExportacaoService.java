package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.document.relatorio.DocumentoRelatorio;
import br.com.fatec.backend.document.relatorio.RelatorioDocxRenderer;
import br.com.fatec.backend.document.relatorio.RelatorioPdfRenderer;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.FormatoRelatorio;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.service.NotificacaoService;
import br.com.fatec.backend.service.UsuarioAutenticadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;

/**
 * Orquestra a exportação dos relatórios: monta os dados, converte para o modelo de documento,
 * renderiza no formato pedido e registra uma notificação para o usuário.
 */
@Service
public class RelatorioExportacaoService {

    public static final String REFERENCIA_TIPO = "RELATORIO";

    private static final Logger log = LoggerFactory.getLogger(RelatorioExportacaoService.class);

    private final RelatorioExtracaoGradeService relatorioExtracaoGradeService;
    private final RelatorioFolhaPontoService relatorioFolhaPontoService;
    private final RelatorioDocumentoMontador montador;
    private final RelatorioPdfRenderer pdfRenderer;
    private final RelatorioDocxRenderer docxRenderer;
    private final NotificacaoService notificacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;

    public RelatorioExportacaoService(RelatorioExtracaoGradeService relatorioExtracaoGradeService,
                                      RelatorioFolhaPontoService relatorioFolhaPontoService,
                                      RelatorioDocumentoMontador montador,
                                      RelatorioPdfRenderer pdfRenderer,
                                      RelatorioDocxRenderer docxRenderer,
                                      NotificacaoService notificacaoService,
                                      UsuarioAutenticadoService usuarioAutenticadoService,
                                      UsuarioRepository usuarioRepository) {
        this.relatorioExtracaoGradeService = relatorioExtracaoGradeService;
        this.relatorioFolhaPontoService = relatorioFolhaPontoService;
        this.montador = montador;
        this.pdfRenderer = pdfRenderer;
        this.docxRenderer = docxRenderer;
        this.notificacaoService = notificacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioRepository = usuarioRepository;
    }

    public ArquivoRelatorio exportarExtracaoGrades(String tipoPeriodo, LocalDate dataReferencia, String formatoTexto) {
        FormatoRelatorio formato = FormatoRelatorio.de(formatoTexto);
        RelatorioExtracaoGradeDTO dados = relatorioExtracaoGradeService.gerar(tipoPeriodo, dataReferencia);

        Long usuarioId = usuarioAutenticadoService.idUsuarioAtual().orElse(null);
        DocumentoRelatorio documento = montador.extracaoGrades(dados, nomeUsuario(usuarioId));
        String nomeArquivo = String.format("Relatorio_Extracao_Grades_%s_%s.%s",
                dados.periodo().tipo().name(), sufixo(dados.periodo().dataInicio(), dados.periodo().dataFim(), dados.periodo().tipo().name()),
                formato.getExtensao());

        ArquivoRelatorio arquivo = new ArquivoRelatorio(renderizar(documento, formato), nomeArquivo, formato.getMediaType());
        notificar(usuarioId, "Relatório de extração de grades gerado",
                String.format("O relatório de extração de grades (%s) foi gerado em %s. Total de grades extraídas: %d.",
                        dados.periodo().descricao(), formato.name(), dados.resumo().gradesExtraidas()));
        return arquivo;
    }

    public ArquivoRelatorio exportarFolhasPonto(String tipoPeriodo, LocalDate dataReferencia, Long professorId,
                                                String formatoTexto) {
        FormatoRelatorio formato = FormatoRelatorio.de(formatoTexto);
        RelatorioFolhaPontoDTO dados = relatorioFolhaPontoService.gerar(tipoPeriodo, dataReferencia, professorId);

        Long usuarioId = usuarioAutenticadoService.idUsuarioAtual().orElse(null);
        DocumentoRelatorio documento = montador.folhasPonto(dados, nomeUsuario(usuarioId));
        String escopo = dados.professor() != null
                ? "Professor_" + paraNomeArquivo(dados.professor().nome())
                : "Todos_Professores";
        String nomeArquivo = String.format("Relatorio_Folhas_Ponto_%s_%s_%s.%s",
                escopo, dados.periodo().tipo().name(),
                sufixo(dados.periodo().dataInicio(), dados.periodo().dataFim(), dados.periodo().tipo().name()),
                formato.getExtensao());

        ArquivoRelatorio arquivo = new ArquivoRelatorio(renderizar(documento, formato), nomeArquivo, formato.getMediaType());
        String alvo = dados.professor() != null ? "de " + dados.professor().nome() : "de todos os professores";
        notificar(usuarioId, "Relatório de folhas de ponto gerado",
                String.format("O relatório de folhas de ponto %s (%s) foi gerado em %s. Folhas no período: %d.",
                        alvo, dados.periodo().descricao(), formato.name(), dados.resumo().totalFolhas()));
        return arquivo;
    }

    private byte[] renderizar(DocumentoRelatorio documento, FormatoRelatorio formato) {
        return switch (formato) {
            case PDF -> pdfRenderer.renderizar(documento);
            case DOCX -> docxRenderer.renderizar(documento);
        };
    }

    private void notificar(Long usuarioId, String titulo, String mensagem) {
        try {
            notificacaoService.notificarUsuario(usuarioId, TipoNotificacao.RELATORIO_GERADO, titulo, mensagem,
                    REFERENCIA_TIPO, null);
        } catch (RuntimeException e) {
            log.warn("Não foi possível registrar a notificação do relatório: {}", e.getClass().getSimpleName());
        }
    }

    private String nomeUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId).map(Usuario::getNome).orElse(null);
    }

    private static String sufixo(LocalDate inicio, LocalDate fim, String tipo) {
        return switch (tipo) {
            case "DIARIO" -> inicio.toString();
            case "MENSAL" -> String.format("%d-%02d", inicio.getYear(), inicio.getMonthValue());
            default -> inicio + "_a_" + fim;
        };
    }

    static String paraNomeArquivo(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String limpo = semAcento.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return limpo.length() > 60 ? limpo.substring(0, 60) : limpo;
    }
}
