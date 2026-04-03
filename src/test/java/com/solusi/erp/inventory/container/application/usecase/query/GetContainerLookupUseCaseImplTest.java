package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetContainerLookupUseCase Tests")
class GetContainerLookupUseCaseImplTest {

    @Mock
    private ContainerRepository repository;

    private GetContainerLookupUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetContainerLookupUseCaseImpl(repository);
    }

    private Container buildContainer(Long id, Long gridId, String gridName) {
        return new Container(
                new AuditMetadata(id, 1L, null, null, null, null),
                gridId, gridName, "Warehouse A",
                "C001", "Container 1", "BC001",
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN,
                "Test note", true
        );
    }

    @Test
    @DisplayName("getById returns LookupDto for existing container")
    void getById_returnsDto_whenFound() {
        Container container = buildContainer(1L, 10L, "Grid A");
        when(repository.findById(1L)).thenReturn(Optional.of(container));

        LookupDto result = useCase.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Container 1");
        assertThat(result.subText()).isEqualTo("C001");
        assertThat(result.payload()).containsEntry("gridId", 10L);
        assertThat(result.payload()).containsEntry("gridName", "Grid A");
    }

    @Test
    @DisplayName("getById throws DomainException when container not found")
    void getById_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(99L))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("maps null gridId to 0L and null gridName to empty string in metadata")
    void getById_mapsNullGridFields_toDefaults() {
        Container container = buildContainer(2L, null, null);
        when(repository.findById(2L)).thenReturn(Optional.of(container));

        LookupDto result = useCase.getById(2L);

        assertThat(result.payload()).containsEntry("gridId", 0L);
        assertThat(result.payload()).containsEntry("gridName", "");
    }

    @Test
    @DisplayName("search by keyword and limit returns mapped list")
    void search_byKeywordAndLimit_returnsMappedList() {
        Container container = buildContainer(3L, 5L, "Grid B");
        when(repository.search("cont", 10)).thenReturn(List.of(container));

        List<LookupDto> result = useCase.search("cont", 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Container 1");
    }

    @Test
    @DisplayName("search with gridId and facilityId returns filtered list")
    void search_withGridAndFacilityId_returnsMappedList() {
        Container container = buildContainer(4L, 7L, "Grid C");
        when(repository.search("cont", 7L, 2L, 5)).thenReturn(List.of(container));

        List<LookupDto> result = useCase.search("cont", 7L, 2L, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).payload()).containsEntry("gridId", 7L);
    }

    @Test
    @DisplayName("findAll delegates to search with empty keyword and limit 10000")
    void findAll_delegatesToSearchWithHighLimit() {
        when(repository.search("", 10000)).thenReturn(List.of());

        List<LookupDto> result = useCase.findAll();

        assertThat(result).isEmpty();
    }
}
