package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.folha_ponto.FolhaPontoPreviewDTO;
import br.com.fatec.backend.dto.folha_ponto.FolhaPontoRequisicaoDTO;
import br.com.fatec.backend.dto.folha_ponto.ProfessorResumoDTO;
import br.com.fatec.backend.service.FolhaPontoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FolhaPontoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FolhaPontoService folhaPontoService;

    @InjectMocks
    private FolhaPontoController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve retornar preview da folha de ponto via POST")
    void deveRetornarPreviewPost() throws Exception {
        ProfessorResumoDTO prof = new ProfessorResumoDTO(1L, "Katia", "100", "123", "CLT", "INDETERMINADO");
        FolhaPontoPreviewDTO preview = new FolhaPontoPreviewDTO(
                prof, 9, 2026, "FOLHA DE FREQUÊNCIA – 09/2026", List.of(), List.of(), List.of()
        );

        when(folhaPontoService.montarPreview(any())).thenReturn(preview);

        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 10L, 9, 2026);

        mockMvc.perform(post("/api/folhas-ponto/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.titulo").value("FOLHA DE FREQUÊNCIA – 09/2026"))
                .andExpect(jsonPath("$.dados.professor.nome").value("Katia"));
    }

    @Test
    @DisplayName("Deve retornar download do DOCX via POST")
    void deveRetornarDocxPost() throws Exception {
        byte[] docxFake = new byte[]{80, 75, 3, 4, 10, 20};
        when(folhaPontoService.gerarFolhaPonto(any())).thenReturn(docxFake);

        FolhaPontoRequisicaoDTO req = new FolhaPontoRequisicaoDTO(1L, 10L, 9, 2026);

        mockMvc.perform(post("/api/folhas-ponto/gerar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Folha_de_Ponto_Professor_1_09_2026.docx\""))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(content().bytes(docxFake));
    }

    @Test
    @DisplayName("Deve permitir download direto via GET")
    void devePermitirDownloadViaGet() throws Exception {
        byte[] docxFake = new byte[]{80, 75, 3, 4, 10, 20};
        when(folhaPontoService.gerarFolhaPonto(any())).thenReturn(docxFake);

        mockMvc.perform(get("/api/folhas-ponto/gerar")
                        .param("professorId", "1")
                        .param("mes", "9")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(docxFake));
    }
}
