package br.com.fatec.backend.controller;

import br.com.fatec.backend.exception.GlobalExceptionHandler;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.service.relatorio.ArquivoRelatorio;
import br.com.fatec.backend.service.relatorio.RelatorioExportacaoService;
import br.com.fatec.backend.service.relatorio.RelatorioExtracaoGradeService;
import br.com.fatec.backend.service.relatorio.RelatorioFolhaPontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RelatorioExtracaoGradeService relatorioExtracaoGradeService;
    @Mock
    private RelatorioFolhaPontoService relatorioFolhaPontoService;
    @Mock
    private RelatorioExportacaoService exportacaoService;

    @InjectMocks
    private RelatorioController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Exportação devolve o arquivo com content-type e nome corretos")
    void exportarPdf() throws Exception {
        when(exportacaoService.exportarFolhasPonto(eq("MENSAL"), eq(LocalDate.of(2026, 11, 1)), eq(1L), eq("PDF")))
                .thenReturn(new ArquivoRelatorio(new byte[]{37, 80, 68, 70}, "Relatorio.pdf", "application/pdf"));

        mockMvc.perform(get("/api/relatorios/folhas-ponto/exportar")
                        .param("periodo", "MENSAL").param("data", "2026-11-01")
                        .param("professorId", "1").param("formato", "PDF"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Relatorio.pdf\""));
    }

    @Test
    @DisplayName("Data em formato inválido responde 400 com mensagem padronizada")
    void dataInvalida() throws Exception {
        mockMvc.perform(get("/api/relatorios/extracoes-grade").param("periodo", "DIARIO").param("data", "18-11-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Valor inválido para o parâmetro 'data'."));
    }

    @Test
    @DisplayName("Período ausente responde 400")
    void periodoAusente() throws Exception {
        mockMvc.perform(get("/api/relatorios/extracoes-grade"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O parâmetro 'periodo' é obrigatório."));
    }

    @Test
    @DisplayName("Regra de negócio do período (ex: data futura) responde 400")
    void regraNegocio() throws Exception {
        when(relatorioExtracaoGradeService.gerar(eq("MENSAL"), any()))
                .thenThrow(new RegraNegocioException("A data de referência não pode ser futura."));

        mockMvc.perform(get("/api/relatorios/extracoes-grade").param("periodo", "MENSAL").param("data", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A data de referência não pode ser futura."));
    }
}
