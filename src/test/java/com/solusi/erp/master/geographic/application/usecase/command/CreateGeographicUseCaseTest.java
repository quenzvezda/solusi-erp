package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.shared.model.GeographicType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGeographicUseCase Tests")
class CreateGeographicUseCaseTest {

    @Mock
    private GeographicRepository repository;

    private CreateGeographicUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateGeographicUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute creates and saves geographic when code is unique")
    void execute_createsAndSavesGeographicWhenCodeIsUnique() {
        when(repository.existsByCode("ID")).thenReturn(false);
        when(repository.save(any(Geographic.class))).thenAnswer(inv -> inv.getArgument(0));

        Geographic result = useCase.execute("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);

        assertThat(result.getCode()).isEqualTo("ID");
        assertThat(result.getName()).isEqualTo("Indonesia");
        assertThat(result.getType()).isEqualTo(GeographicType.COUNTRY);
    }

    @Test
    @DisplayName("execute returns persisted geographic with id from repository")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        Geographic persisted = new Geographic(metadata, "ID", "Indonesia",
                GeographicType.COUNTRY, null, null, Boolean.TRUE);

        when(repository.existsByCode("ID")).thenReturn(false);
        when(repository.save(any(Geographic.class))).thenReturn(persisted);

        Geographic result = useCase.execute("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists")
    void execute_throwsDomainException_whenCodeDuplicate() {
        when(repository.existsByCode("ID")).thenReturn(true);

        assertThrows(DomainException.class, () ->
                useCase.execute("ID", "Indonesia", GeographicType.COUNTRY, null, null, Boolean.TRUE));
    }
}

