package br.com.fatec.backend.service.extracao;

import br.com.fatec.backend.config.GeminiProperties;
import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.exception.ExtracaoIndisponivelException;
import br.com.fatec.backend.exception.RegraNegocioException;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GeminiExtratorDeGrade implements ExtratorDeGrade {

    private static final Logger log = LoggerFactory.getLogger(GeminiExtratorDeGrade.class);

    private static final String PROMPT = """
            Você extrai dados de Grades Horárias de docentes do Centro Paula Souza (Fatec).
            Leia o PDF anexo e responda SOMENTE com um objeto JSON, sem texto adicional,
            exatamente neste formato:

            {
              "semestre": 1,
              "ano": 2026,
              "validadeInicio": "2026-03-07",
              "validadeFim": "2026-03-15",
              "professor": { "nome": "", "matricula": "", "cpf": "", "contrato": "" },
              "horas": { "semanalAula": 0, "mensalAula": 0, "mensalAtividade": 0,
                         "totalSemanal": 0, "totalMensal": 0 },
              "disciplinas": [
                {
                  "nome": "", "curso": "", "periodo": "", "status": "",
                  "quantidadeAulas": 0, "op": 0,
                  "aulas": [ { "diaSemana": 0, "horaInicio": "", "horaFim": "" } ]
                }
              ]
            }

            Regras:
            - Não invente dados. Campo que não existir no PDF deve ser null; listas sem itens, [].
            - semestre e ano vêm do campo SEMESTRE (ex.: "1º/2026" => 1 e 2026).
            - Datas no formato yyyy-MM-dd.
            - professor.cpf: somente dígitos. professor.contrato: DETERMINADO, INDETERMINADO
              ou TEMPORARIO (maiúsculas, sem acento).
            - Crie um item em "disciplinas" para cada linha da tabela "Horas Aulas".
            - disciplinas.nome: sem o sufixo entre parênteses (ex.: "ESPANHOL I (Determinada)" => "ESPANHOL I").
            - disciplinas.curso: como impresso na tabela.
            - disciplinas.periodo: valor da coluna "Período" da tabela de disciplinas, como
              MANHA, TARDE ou NOITE. NÃO deduza o período pelo horário das aulas.
            - disciplinas.status: MINISTRANDO ou CANCELADA. quantidadeAulas: coluna "Qtde".
            - disciplinas.op: número após "OP:" nos blocos de horário da disciplina.
            - aulas: um item para cada bloco de horário (ex.: "07:30 | 08:20") das grades
              semanais Manhã/Tarde/Noite, associado à disciplina pelo nome impresso no bloco.
              Mantenha cada bloco separado, exatamente como impresso (07:30|08:20 e 08:20|09:10
              são DOIS itens). Não una nem divida blocos.
            - aulas.diaSemana: determinado pela COLUNA do cabeçalho em que o bloco está:
              SEGUNDA=1, TERÇA=2, QUARTA=3, QUINTA=4, SEXTA=5, SÁBADO=6, DOMINGO=7.
            - Horários em HH:mm, formato 24h.
            - horas: use a tabela de atividades no rodapé. semanalAula e mensalAula = linha
              HORA AULA (colunas Semanal e Mensal); mensalAtividade = coluna Mensal da linha
              HORA ATIVIDADE; totalSemanal e totalMensal = linha Total. Decimais com ponto.
            - Ignore assinaturas, "De acordo", "Aprovado" e a lista de HAE/RJI sem valores.
            """;

    private final GeminiProperties props;
    private final JsonMapper jsonMapper;
    private volatile Client client;

    public GeminiExtratorDeGrade(GeminiProperties props, JsonMapper jsonMapper) {
        this.props = props;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public GradeExtraidaDTO extrair(byte[] pdf) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new ExtracaoIndisponivelException(
                    "O serviço de extração não está configurado (GEMINI_API_KEY ausente).");
        }

        String texto = chamarGemini(pdf);

        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException(
                    "Não foi possível extrair dados do arquivo. Verifique se é uma grade válida e tente novamente.");
        }

        try {
            return jsonMapper.readValue(limparCercas(texto), GradeExtraidaDTO.class);
        } catch (JacksonException e) {
            // Não loga o conteúdo: contém nome e CPF do professor.
            log.warn("Resposta do Gemini fora do formato esperado: {}", e.getClass().getSimpleName());
            throw new RegraNegocioException(
                    "Não foi possível interpretar a grade enviada. Tente novamente ou verifique o arquivo.");
        }
    }

    private String chamarGemini(byte[] pdf) {
        try {
            Content conteudo = Content.fromParts(
                    Part.fromBytes(pdf, "application/pdf"),
                    Part.fromText(PROMPT));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .httpOptions(HttpOptions.builder()
                            .timeout(props.timeoutSegundos() * 1000)
                            .build())
                    .build();

            GenerateContentResponse resposta =
                    client().models.generateContent(props.model(), conteudo, config);
            return resposta.text();

        } catch (ApiException e) {
            log.error("Gemini retornou erro HTTP {}: {}", e.code(), e.getMessage());
            String msg = e.code() == 429
                    ? "Limite de uso do serviço de extração atingido. Tente novamente em instantes."
                    : "O serviço de extração está indisponível no momento. Tente novamente mais tarde.";
            throw new ExtracaoIndisponivelException(msg);
        } catch (RuntimeException e) {
            log.error("Falha ao chamar o Gemini", e);
            throw new ExtracaoIndisponivelException(
                    "Não foi possível se comunicar com o serviço de extração. Tente novamente mais tarde.");
        }
    }

    /** Cria o cliente só no primeiro uso, para a aplicação subir mesmo sem a chave. */
    private Client client() {
        Client c = client;
        if (c == null) {
            synchronized (this) {
                c = client;
                if (c == null) {
                    c = Client.builder().apiKey(props.apiKey()).build();
                    client = c;
                }
            }
        }
        return c;
    }

    /** Defesa: remove cercas ```json caso o modelo as inclua. */
    private String limparCercas(String texto) {
        String t = texto.strip();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("\\s*```$", "");
        }
        return t.strip();
    }

    @PreDestroy
    void fechar() {
        if (client != null) {
            client.close();
        }
    }
}