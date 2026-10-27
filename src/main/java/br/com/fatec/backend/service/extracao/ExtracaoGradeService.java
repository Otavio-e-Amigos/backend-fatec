package br.com.fatec.backend.service.extracao;

import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.service.extracao.ValidadorGradeExtraida.Resultado;
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

    public ExtracaoGradeService(ExtratorDeGrade extrator, ValidadorGradeExtraida validador) {
        this.extrator = extrator;
        this.validador = validador;
    }

    public GradeExtraidaDTO extrair(MultipartFile arquivo) {
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

        Resultado r = validador.processar(extrator.extrair(bytes));
        return r.dados().comAvisos(r.problemas());   // problemas viram avisos
    }

    public GradeExtraidaDTO validar(GradeExtraidaDTO dados) {
        Resultado r = validador.processar(dados);
        if (!r.problemas().isEmpty()) {
            throw new RegraNegocioException("Dados inválidos: " + String.join("; ", r.problemas()));
        }
        return r.dados();
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