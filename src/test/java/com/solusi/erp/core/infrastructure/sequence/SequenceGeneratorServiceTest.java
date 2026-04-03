package com.solusi.erp.core.infrastructure.sequence;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SequenceGeneratorServiceTest {

    private final SystemSequenceRepository repository = mock(SystemSequenceRepository.class);
    private final SequenceGeneratorService service = new SequenceGeneratorService(repository);

    @Test
    void generate_incrementsValueAndFormatsTemplate() {
        var seq = new SystemSequence();
        seq.setModuleCode("INV");
        seq.setFormatPattern("INV-{seq}");
        seq.setCurrentValue(5L);
        seq.setPadLength(4);
        seq.setResetCycle(SystemSequence.ResetCycle.NEVER);
        seq.setLastResetDate(LocalDateTime.now());

        when(repository.findById("INV")).thenReturn(Optional.of(seq));

        String result = service.generate("INV");

        assertThat(result).isEqualTo("INV-0006");
        assertThat(seq.getCurrentValue()).isEqualTo(6L);
        verify(repository).save(seq);
    }

    @Test
    void generate_withDatePlaceholder_resolvesDate() {
        var seq = new SystemSequence();
        seq.setModuleCode("PO");
        seq.setFormatPattern("PO/{date:yyyy}/{seq}");
        seq.setCurrentValue(0L);
        seq.setPadLength(3);
        seq.setResetCycle(SystemSequence.ResetCycle.NEVER);
        seq.setLastResetDate(LocalDateTime.now());

        when(repository.findById("PO")).thenReturn(Optional.of(seq));

        String result = service.generate("PO");

        String expectedYear = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        assertThat(result).isEqualTo("PO/" + expectedYear + "/001");
    }

    @Test
    void generate_resetsOnDailyCycle() {
        var yesterday = LocalDateTime.now().minusDays(1);
        var seq = new SystemSequence();
        seq.setModuleCode("DAILY");
        seq.setFormatPattern("D-{seq}");
        seq.setCurrentValue(99L);
        seq.setPadLength(3);
        seq.setResetCycle(SystemSequence.ResetCycle.DAILY);
        seq.setLastResetDate(yesterday);

        when(repository.findById("DAILY")).thenReturn(Optional.of(seq));

        String result = service.generate("DAILY");

        // After reset, currentValue goes to 0, then incremented to 1
        assertThat(result).isEqualTo("D-001");
        assertThat(seq.getCurrentValue()).isEqualTo(1L);
    }

    @Test
    void generate_resetsOnMonthlyCycle() {
        var lastMonth = LocalDateTime.now().minusMonths(1);
        var seq = new SystemSequence();
        seq.setModuleCode("MONTHLY");
        seq.setFormatPattern("M-{seq}");
        seq.setCurrentValue(50L);
        seq.setPadLength(4);
        seq.setResetCycle(SystemSequence.ResetCycle.MONTHLY);
        seq.setLastResetDate(lastMonth);

        when(repository.findById("MONTHLY")).thenReturn(Optional.of(seq));

        String result = service.generate("MONTHLY");

        assertThat(result).isEqualTo("M-0001");
        assertThat(seq.getCurrentValue()).isEqualTo(1L);
    }

    @Test
    void generate_resetsOnYearlyCycle() {
        var lastYear = LocalDateTime.now().minusYears(1);
        var seq = new SystemSequence();
        seq.setModuleCode("YEARLY");
        seq.setFormatPattern("Y-{seq}");
        seq.setCurrentValue(200L);
        seq.setPadLength(5);
        seq.setResetCycle(SystemSequence.ResetCycle.YEARLY);
        seq.setLastResetDate(lastYear);

        when(repository.findById("YEARLY")).thenReturn(Optional.of(seq));

        String result = service.generate("YEARLY");

        assertThat(result).isEqualTo("Y-00001");
        assertThat(seq.getCurrentValue()).isEqualTo(1L);
    }

    @Test
    void generate_neverResetCycle_doesNotReset() {
        var longAgo = LocalDateTime.now().minusYears(5);
        var seq = new SystemSequence();
        seq.setModuleCode("NEVER");
        seq.setFormatPattern("N-{seq}");
        seq.setCurrentValue(100L);
        seq.setPadLength(4);
        seq.setResetCycle(SystemSequence.ResetCycle.NEVER);
        seq.setLastResetDate(longAgo);

        when(repository.findById("NEVER")).thenReturn(Optional.of(seq));

        String result = service.generate("NEVER");

        assertThat(result).isEqualTo("N-0101");
        assertThat(seq.getCurrentValue()).isEqualTo(101L);
    }

    @Test
    void generate_moduleNotFound_throwsException() {
        when(repository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate("UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void generate_nullLastResetDate_initializesIt() {
        var seq = new SystemSequence();
        seq.setModuleCode("NEW");
        seq.setFormatPattern("NEW-{seq}");
        seq.setCurrentValue(0L);
        seq.setPadLength(3);
        seq.setResetCycle(SystemSequence.ResetCycle.DAILY);
        seq.setLastResetDate(null);

        when(repository.findById("NEW")).thenReturn(Optional.of(seq));

        String result = service.generate("NEW");

        assertThat(result).isEqualTo("NEW-001");
        assertThat(seq.getLastResetDate()).isNotNull();
    }
}
