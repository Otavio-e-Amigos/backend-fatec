package br.com.fatec.backend.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotGradeTest {

    @Test
    @DisplayName("Deve converter horários conforme exemplos oficiais da documentação técnica")
    void deveConverterHorariosOficiais() {
        // 07:30 exato -> 1º Manhã
        assertEquals(SlotGrade.MANHA_1, SlotGrade.converter(LocalTime.of(7, 30)));

        // 08:00 (distância 30 para 07:30 e 20 para 08:20) -> 2º Manhã
        assertEquals(SlotGrade.MANHA_2, SlotGrade.converter(LocalTime.of(8, 0)));

        // 08:50 (distância 30 para 08:20 e 30 para 09:20 - empate) -> 3º Manhã
        assertEquals(SlotGrade.MANHA_3, SlotGrade.converter(LocalTime.of(8, 50)));

        // 09:50 (distância 30 para 09:20 e 20 para 10:10) -> 4º Manhã
        assertEquals(SlotGrade.MANHA_4, SlotGrade.converter(LocalTime.of(9, 50)));

        // 10:40 (distância 30 para 10:10 e 30 para 11:10 - empate) -> 5º Manhã
        assertEquals(SlotGrade.MANHA_5, SlotGrade.converter(LocalTime.of(10, 40)));

        // 12:30 (distância 30 para 12:00 e 30 para 13:00 - empate entre turnos) -> 1º Tarde
        assertEquals(SlotGrade.TARDE_1, SlotGrade.converter(LocalTime.of(12, 30)));

        // 18:25 (distância 55 para 17:30 e 55 para 19:20 - empate entre turnos) -> 1º Noite
        assertEquals(SlotGrade.NOITE_1, SlotGrade.converter(LocalTime.of(18, 25)));

        // 22:00 exato -> 4º Noite
        assertEquals(SlotGrade.NOITE_4, SlotGrade.converter(LocalTime.of(22, 0)));
    }

    @Test
    @DisplayName("Deve expandir bloco contínuo de aula em múltiplos slots")
    void deveExpandirBlocoContinuo() {
        // Tarde completa: 13:00 às 18:20 -> 6 slots da tarde
        List<SlotGrade> slotsTarde = SlotGrade.calcularSlots(Turno.TARDE, LocalTime.of(13, 0), LocalTime.of(18, 20));
        assertEquals(6, slotsTarde.size());
        assertEquals(SlotGrade.TARDE_1, slotsTarde.get(0));
        assertEquals(SlotGrade.TARDE_6, slotsTarde.get(5));

        // Noite: 19:20 às 21:00 -> 2 slots da noite
        List<SlotGrade> slotsNoite = SlotGrade.calcularSlots(Turno.NOITE, LocalTime.of(19, 20), LocalTime.of(21, 0));
        assertEquals(2, slotsNoite.size());
        assertEquals(SlotGrade.NOITE_1, slotsNoite.get(0));
        assertEquals(SlotGrade.NOITE_2, slotsNoite.get(1));
    }
}
