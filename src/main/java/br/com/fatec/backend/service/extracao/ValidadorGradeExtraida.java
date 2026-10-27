package br.com.fatec.backend.service.extracao;

import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO.*;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Normaliza os dados extraídos e lista os problemas encontrados. Sem acesso ao banco. */
@Component
public class ValidadorGradeExtraida {

    public record Resultado(GradeExtraidaDTO dados, List<String> problemas) { }

    private record Bloco(String rotulo, int dia, LocalTime inicio, LocalTime fim) { }

    private static final Set<String> PERIODOS = Set.of("MANHA", "TARDE", "NOITE");
    private static final Set<String> CONTRATOS = Set.of("DETERMINADO", "INDETERMINADO", "TEMPORARIO");
    private static final Set<String> STATUS = Set.of("MINISTRANDO", "CANCELADA");
    private static final String[] DIAS = {"", "segunda", "terça", "quarta", "quinta", "sexta", "sábado", "domingo"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("H:mm");

    public Resultado processar(GradeExtraidaDTO in) {
        List<String> problemas = new ArrayList<>();

        // Período letivo
        if (in.semestre() == null || in.semestre() < 1 || in.semestre() > 2) {
            problemas.add("Semestre inválido (deve ser 1 ou 2).");
        }
        if (in.ano() == null || in.ano() < 2000 || in.ano() > 2100) {
            problemas.add("Ano inválido.");
        }
        LocalDate ini = data(in.validadeInicio(), "Validade início", problemas);
        LocalDate fim = data(in.validadeFim(), "Validade fim", problemas);
        if (ini != null && fim != null && fim.isBefore(ini)) {
            problemas.add("A validade fim não pode ser anterior à validade início.");
        }

        // Professor
        ProfessorExtraido prof = null;
        if (in.professor() == null) {
            problemas.add("Professor não identificado.");
        } else {
            ProfessorExtraido p = in.professor();
            String nome = limpar(p.nome());
            String matricula = limpar(p.matricula());
            String cpf = p.cpf() == null ? null : p.cpf().replaceAll("\\D", "");
            if (nome == null) problemas.add("Nome do professor não informado.");
            if (matricula == null) problemas.add("Matrícula do professor não informada.");
            if (cpf == null || cpf.length() != 11) problemas.add("CPF do professor inválido (deve ter 11 dígitos).");
            prof = new ProfessorExtraido(nome, matricula, cpf,
                    enumValor(p.contrato(), CONTRATOS, "Contrato", problemas));
        }

        // Disciplinas e aulas
        List<DisciplinaExtraida> disciplinas = new ArrayList<>();
        List<Bloco> blocos = new ArrayList<>();
        if (in.disciplinas().isEmpty()) {
            problemas.add("Nenhuma disciplina encontrada.");
        }

        int n = 0;
        for (DisciplinaExtraida d : in.disciplinas()) {
            n++;
            String nome = limpar(d.nome() == null ? null : d.nome().replaceAll("\\s*\\([^)]*\\)\\s*$", ""));
            String rotulo = nome != null ? nome : "Disciplina #" + n;
            String curso = limpar(d.curso());

            if (nome == null) problemas.add(rotulo + ": nome não informado.");
            if (curso == null) problemas.add(rotulo + ": curso não informado.");

            String periodo = enumValor(d.periodo(), PERIODOS, rotulo + ": período", problemas);
            String status = enumValor(d.status(), STATUS, rotulo + ": status", problemas);

            List<AulaExtraida> aulas = new ArrayList<>();
            for (AulaExtraida a : d.aulas()) {
                Integer dia = a.diaSemana();
                if (dia == null || dia < 1 || dia > 7) {
                    problemas.add(rotulo + ": dia da semana inválido (deve ser de 1 a 7).");
                }
                LocalTime hi = hora(a.horaInicio(), rotulo + ": hora de início", problemas);
                LocalTime hf = hora(a.horaFim(), rotulo + ": hora de término", problemas);

                if (hi != null && hf != null && !hf.isAfter(hi)) {
                    problemas.add(rotulo + ": hora de término deve ser posterior à de início (" + hi + "–" + hf + ").");
                } else if (dia != null && dia >= 1 && dia <= 7 && hi != null && hf != null) {
                    blocos.add(new Bloco(rotulo, dia, hi, hf));
                }
                aulas.add(new AulaExtraida(dia,
                        hi == null ? a.horaInicio() : hi.toString(),
                        hf == null ? a.horaFim() : hf.toString()));
            }

            Integer qtd = d.quantidadeAulas();
            if (qtd == null) {
                problemas.add(rotulo + ": quantidade de aulas não informada.");
            } else if (qtd != aulas.size()) {
                problemas.add(rotulo + ": informa " + qtd + " aula(s), mas há " + aulas.size() + " horário(s).");
            }

            disciplinas.add(new DisciplinaExtraida(nome, curso, periodo, status, qtd, d.op(), aulas));
        }

        verificarSobreposicao(blocos, problemas);

        GradeExtraidaDTO normalizado = new GradeExtraidaDTO(
                in.semestre(), in.ano(),
                ini == null ? in.validadeInicio() : ini.toString(),
                fim == null ? in.validadeFim() : fim.toString(),
                prof, in.horas(), disciplinas, List.of());

        return new Resultado(normalizado, problemas);
    }

    private void verificarSobreposicao(List<Bloco> blocos, List<String> problemas) {
        for (int i = 0; i < blocos.size(); i++) {
            for (int j = i + 1; j < blocos.size(); j++) {
                Bloco a = blocos.get(i), b = blocos.get(j);
                if (a.dia() == b.dia() && a.inicio().isBefore(b.fim()) && b.inicio().isBefore(a.fim())) {
                    problemas.add("Conflito de horário na " + DIAS[a.dia()] + ": "
                            + a.rotulo() + " (" + a.inicio() + "–" + a.fim() + ") e "
                            + b.rotulo() + " (" + b.inicio() + "–" + b.fim() + ").");
                }
            }
        }
    }

    private String limpar(String v) {
        return v == null || v.isBlank() ? null : v.strip();
    }

    private String enumValor(String v, Set<String> validos, String campo, List<String> problemas) {
        if (v == null || v.isBlank()) {
            problemas.add(campo + " não informado.");
            return null;
        }
        String norm = Normalizer.normalize(v.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase();
        if (!validos.contains(norm)) {
            problemas.add(campo + " inválido: \"" + v + "\".");
        }
        return norm;
    }

    private LocalDate data(String v, String campo, List<String> problemas) {
        if (v == null || v.isBlank()) {
            problemas.add(campo + " não informada.");
            return null;
        }
        try {
            return LocalDate.parse(v.strip());
        } catch (DateTimeParseException e) {
            problemas.add(campo + " inválida (use o formato aaaa-mm-dd).");
            return null;
        }
    }

    private LocalTime hora(String v, String campo, List<String> problemas) {
        if (v == null || v.isBlank()) {
            problemas.add(campo + " não informada.");
            return null;
        }
        try {
            return LocalTime.parse(v.strip(), HORA);
        } catch (DateTimeParseException e) {
            problemas.add(campo + " inválida (use HH:mm).");
            return null;
        }
    }
}