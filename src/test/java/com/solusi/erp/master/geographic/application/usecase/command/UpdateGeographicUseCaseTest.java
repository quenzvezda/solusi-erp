package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.model.GeographicType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateGeographicUseCase Tests")
class UpdateGeographicUseCaseTest {

    @Mock
    private GeographicRepository repository;

    private UpdateGeographicUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateGeographicUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates mutable fields; code remains unchanged")
    void execute_updatesMutableFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Geographic existing = new Geographic(metadata, "ID", "Indonesia",
                GeographicType.COUNTRY, null, null, Boolean.TRUE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Geographic.class))).thenAnswer(inv -> inv.getArgument(0));

        Geographic result = useCase.execute(1L, "Republic of Indonesia",
                GeographicType.COUNTRY, null, null, Boolean.TRUE);

        assertThat(result.getName()).isEqualTo("Republic of Indonesia");
        assertThat(result.getCode()).isEqualTo("ID");
    }

    @Test
    @DisplayName("execute throws DomainException when geographic is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () ->
                useCase.execute(99L, "Name", GeographicType.COUNTRY, null, null, Boolean.TRUE));
    }
}
