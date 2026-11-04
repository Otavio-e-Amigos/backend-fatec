package br.com.fatec.backend.service.extracao;

import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.service.extracao.ValidadorGradeExtraida.Resultado;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ExtracaoGradeService {

    private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;
    private static final byte[] ASSINATURA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final ExtratorDeGrade extrator;
    private final ValidadorGradeExtraida validador;
    private final UsuarioRepository usuarioRepository;

    public ExtracaoGradeService(ExtratorDeGrade extrator,
                                ValidadorGradeExtraida validador,
                                UsuarioRepository usuarioRepository) {
        this.extrator = extrator;
        this.validador = validador;
        this.usuarioRepository = usuarioRepository;
    }

    public GradeExtraidaDTO extrair(MultipartFile arquivo) {
        return extrair(arquivo, null);
    }

    public GradeExtraidaDTO extrair(MultipartFile arquivo, String apiKeyInformada) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("O arquivo enviado está vazio.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new RegraNegocioException("O arquivo excede o tamanho máximo de 10 MB.");
        }

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo enviado.");
        }

        if (!comecaComAssinaturaPdf(bytes)) {
            throw new RegraNegocioException("Formato inválido: envie um arquivo PDF.");
        }

        String apiKeyEfetiva = resolverApiKey(apiKeyInformada);

        Resultado r = validador.processar(extrator.extrair(bytes, apiKeyEfetiva));
        return r.dados().comAvisos(r.problemas());
    }

    public GradeExtraidaDTO validar(GradeExtraidaDTO dados) {
        Resultado r = validador.processar(dados);
        if (!r.problemas().isEmpty()) {
            throw new RegraNegocioException("Dados inválidos: " + String.join("; ", r.problemas()));
        }
        return r.dados();
    }

    private String resolverApiKey(String apiKeyInformada) {
        if (apiKeyInformada != null && !apiKeyInformada.isBlank()) {
            return apiKeyInformada.trim();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            try {
                Long usuarioId = Long.parseLong(auth.getPrincipal().toString());
                return usuarioRepository.findById(usuarioId)
                        .map(Usuario::getGeminiApiKey)
                        .filter(k -> !k.isBlank())
                        .orElse(null);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private boolean comecaComAssinaturaPdf(byte[] bytes) {
        if (bytes.length < ASSINATURA_PDF.length) {
            return false;
        }
        for (int i = 0; i < ASSINATURA_PDF.length; i++) {
            if (bytes[i] != ASSINATURA_PDF[i]) {
                return false;
            }
        }
        return true;
    }
}
