package br.com.fatec.backend.service;

import br.com.fatec.backend.config.GeminiProperties;
import br.com.fatec.backend.dto.usuario.GeminiApiKeyValidacaoDTO;
import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeminiApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiKeyService.class);

    private final GeminiProperties props;

    public GeminiApiKeyService(GeminiProperties props) {
        this.props = props;
    }

    /**
     * Valida se a chave fornecida funciona com o modelo configurado.
     * Faz uma chamada mínima ao Gemini para verificar.
     */
    public GeminiApiKeyValidacaoDTO validar(String apiKey) {
        try (Client client = Client.builder().apiKey(apiKey).build()) {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("text/plain")
                    .httpOptions(HttpOptions.builder().timeout(15_000).build())
                    .build();

            GenerateContentResponse resposta = client.models.generateContent(
                    props.model(),
                    "Responda apenas: OK",
                    config);

            String texto = resposta.text();
            if (texto != null && !texto.isBlank()) {
                return new GeminiApiKeyValidacaoDTO(true,
                        "Chave válida! O modelo '" + props.model() + "' respondeu com sucesso.", props.model());
            }
            return new GeminiApiKeyValidacaoDTO(false, "A chave não gerou resposta.", null);

        } catch (ApiException e) {
            log.warn("Validação de API key falhou: HTTP {}", e.code());
            String msg = switch (e.code()) {
                case 400 -> "Requisição inválida. Verifique o formato da chave.";
                case 401, 403 -> "Chave inválida ou sem permissão para usar o modelo '" + props.model() + "'. Verifique se a chave está correta e se o modelo está disponível.";
                case 429 -> "Cota da chave atingida no momento da validação. A chave parece válida, mas o limite de uso foi atingido.";
                case 503 -> "Serviço do Gemini temporariamente indisponível. Tente novamente em instantes.";
                default -> "Erro ao validar chave (código " + e.code() + "): " + e.getMessage();
            };
            return new GeminiApiKeyValidacaoDTO(false, msg, null);
        } catch (Exception e) {
            log.error("Erro inesperado ao validar API key do Gemini", e);
            return new GeminiApiKeyValidacaoDTO(false,
                    "Não foi possível conectar ao serviço do Gemini. Verifique sua conexão.", null);
        }
    }
}
