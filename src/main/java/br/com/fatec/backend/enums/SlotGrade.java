package br.com.fatec.backend.enums;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa os slots (blocos de aula) oficiais do modelo da Folha de Ponto/Frequência da FATEC.
 * Contém horários de referência para conversão de aulas reais cadastradas.
 */
public enum SlotGrade {

    // --- MANHÃ ---
    MANHA_1(Turno.MANHA, 1, LocalTime.of(7, 30), LocalTime.of(8, 20), 1),
    MANHA_2(Turno.MANHA, 2, LocalTime.of(8, 20), LocalTime.of(9, 10), 2),
    MANHA_3(Turno.MANHA, 3, LocalTime.of(9, 20), LocalTime.of(10, 10), 3),
    MANHA_4(Turno.MANHA, 4, LocalTime.of(10, 10), LocalTime.of(11, 0), 4),
    MANHA_5(Turno.MANHA, 5, LocalTime.of(11, 10), LocalTime.of(12, 0), 5),
    MANHA_6(Turno.MANHA, 6, LocalTime.of(12, 0), LocalTime.of(12, 50), 6),

    // --- TARDE ---
    TARDE_1(Turno.TARDE, 1, LocalTime.of(13, 0), LocalTime.of(13, 50), 7),
    TARDE_2(Turno.TARDE, 2, LocalTime.of(13, 50), LocalTime.of(14, 40), 8),
    TARDE_3(Turno.TARDE, 3, LocalTime.of(14, 50), LocalTime.of(15, 40), 9),
    TARDE_4(Turno.TARDE, 4, LocalTime.of(15, 40), LocalTime.of(16, 30), 10),
    TARDE_5(Turno.TARDE, 5, LocalTime.of(16, 40), LocalTime.of(17, 30), 11),
    TARDE_6(Turno.TARDE, 6, LocalTime.of(17, 30), LocalTime.of(18, 20), 12),

    // --- NOITE ---
    NOITE_1(Turno.NOITE, 1, LocalTime.of(19, 20), LocalTime.of(20, 10), 13),
    NOITE_2(Turno.NOITE, 2, LocalTime.of(20, 10), LocalTime.of(21, 0), 14),
    NOITE_3(Turno.NOITE, 3, LocalTime.of(21, 10), LocalTime.of(22, 0), 15),
    NOITE_4(Turno.NOITE, 4, LocalTime.of(22, 0), LocalTime.of(22, 50), 16);

    private final Turno turno;
    private final int ordemNoTurno;
    private final LocalTime horaInicio;
    private final LocalTime horaFim;
    private final int colunaGrid;

    SlotGrade(Turno turno, int ordemNoTurno, LocalTime horaInicio, LocalTime horaFim, int colunaGrid) {
        this.turno = turno;
        this.ordemNoTurno = ordemNoTurno;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.colunaGrid = colunaGrid;
    }

    public Turno getTurno() {
        return turno;
    }

    public int getOrdemNoTurno() {
        return ordemNoTurno;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public int getColunaGrid() {
        return colunaGrid;
    }

    /**
     * Converte um horário de início para o slot mais próximo.
     * Em caso de empate na distância, seleciona o slot POSTERIOR (conforme regra oficial).
     */
    public static SlotGrade converter(LocalTime inicio) {
        if (inicio == null) {
            return null;
        }

        int inicioMinutos = inicio.getHour() * 60 + inicio.getMinute();
        SlotGrade melhor = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (SlotGrade slot : values()) {
            int slotMinutos = slot.horaInicio.getHour() * 60 + slot.horaInicio.getMinute();
            int distancia = Math.abs(slotMinutos - inicioMinutos);

            // "<=" garante que empates escolham o horário posterior
            if (distancia <= menorDistancia) {
                menorDistancia = distancia;
                melhor = slot;
            }
        }

        return melhor;
    }

    /**
     * Converte um horário com restrição ao turno especificado.
     */
    public static SlotGrade converterComTurno(Turno turno, LocalTime inicio) {
        if (inicio == null) {
            return null;
        }
        if (turno == null) {
            return converter(inicio);
        }

        int inicioMinutos = inicio.getHour() * 60 + inicio.getMinute();
        SlotGrade melhor = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (SlotGrade slot : values()) {
            if (slot.getTurno() == turno) {
                int slotMinutos = slot.horaInicio.getHour() * 60 + slot.horaInicio.getMinute();
                int distancia = Math.abs(slotMinutos - inicioMinutos);

                if (distancia <= menorDistancia) {
                    menorDistancia = distancia;
                    melhor = slot;
                }
            }
        }

        return melhor != null ? melhor : converter(inicio);
    }

    /**
     * Calcula a lista de slots ocupados por uma aula.
     * Suporta tanto aulas individuais (~50 min) quanto blocos contínuos longos.
     */
    public static List<SlotGrade> calcularSlots(Turno turno, LocalTime inicio, LocalTime fim) {
        if (inicio == null) {
            return List.of();
        }

        // Se fim não foi informado ou a duração for curta (<= 60 minutos), converte como aula única
        if (fim == null || !fim.isAfter(inicio) || java.time.Duration.between(inicio, fim).toMinutes() <= 60) {
            SlotGrade slot = converterComTurno(turno, inicio);
            return slot != null ? List.of(slot) : List.of();
        }

        // Bloco contínuo: coleta todos os slots do turno que estão contidos no intervalo
        List<SlotGrade> slots = new ArrayList<>();
        for (SlotGrade s : values()) {
            if (turno == null || s.getTurno() == turno) {
                if (!s.horaInicio.isBefore(inicio) && s.horaInicio.isBefore(fim)) {
                    slots.add(s);
                }
            }
        }

        if (slots.isEmpty()) {
            SlotGrade slot = converterComTurno(turno, inicio);
            if (slot != null) {
                slots.add(slot);
            }
        }

        return slots;
    }
}
