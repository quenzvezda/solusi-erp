package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UpdateContainerUseCase Tests")
class UpdateContainerUseCaseTest {

    private InMemoryContainerRepository repository;
    private UpdateContainerUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContainerRepository();
        useCase = new UpdateContainerUseCaseImpl(repository);
    }

    @Test
    void execute_throwsWhenContainerNotFound() {
        assertThatThrownBy(() -> useCase.execute(
                99L, "Rack A", "BC-001", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.TEN, "note", true))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.container.notfound");
    }

    @Test
    void execute_throwsWhenBarcodeAlreadyUsedByAnotherContainer() {
        repository.existing = container();
        repository.duplicateBarcode = true;

        assertThatThrownBy(() -> useCase.execute(
                1L, "Rack A", "BC-001", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.TEN, "note", true))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.container.duplicate-barcode");
    }

    @Test
    void execute_updatesAndSavesWhenBarcodeBlank() {
        repository.existing = container();

        Container updated = useCase.execute(
                1L, "Rack B", " ", BigDecimal.ONE, new BigDecimal("2"),
                new BigDecimal("3"), new BigDecimal("4"), "updated", false);

        assertThat(updated.getName()).isEqualTo("Rack B");
        assertThat(updated.getBarcode()).isBlank();
        assertThat(updated.getWidth()).isEqualByComparingTo("2");
        assertThat(updated.getIsActive()).isFalse();
        assertThat(repository.barcodeChecked).isFalse();
        assertThat(repository.saved).isSameAs(updated);
    }

    @Test
    void execute_updatesAndSavesWhenBarcodeIsUnique() {
        repository.existing = container();

        Container updated = useCase.execute(
                1L, "Rack B", "BC-002", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.TEN, "updated", true);

        assertThat(updated.getBarcode()).isEqualTo("BC-002");
        assertThat(repository.barcodeChecked).isTrue();
        assertThat(repository.saved).isSameAs(updated);
    }

    private Container container() {
        return new Container(
                new AuditMetadata(1L, 1L, null, null, null, null),
                10L, "Grid", "Facility", "CONT-001", "Rack A", "BC-OLD",
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN,
                "old", true
        );
    }

    private static final class InMemoryContainerRepository implements ContainerRepository {
        private Container existing;
        private Container saved;
        private boolean duplicateBarcode;
        private boolean barcodeChecked;

        @Override
        public Container save(Container container) {
            saved = container;
            return container;
        }

        @Override
        public Optional<Container> findById(Long id) {
            return Optional.ofNullable(existing);
        }

        @Override
        public Page<Container> findAll(String keyword, Long gridId, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Container> search(String keyword, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Container> search(String keyword, Long gridId, Long facilityId, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Long id) {
        }

        @Override
        public boolean existsByBarcode(String barcode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByBarcodeAndIdNot(String barcode, Long id) {
            barcodeChecked = true;
            return duplicateBarcode;
        }
    }
}
