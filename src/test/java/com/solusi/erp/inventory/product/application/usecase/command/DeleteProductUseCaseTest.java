package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.port.ProductInUseChecker;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeleteProductUseCaseTest {

    @Test
    void delete_ok_when_not_used() {
        ProductRepository repo = mock(ProductRepository.class);
        ProductInUseChecker checker = mock(ProductInUseChecker.class);

        when(repo.findById(1L)).thenReturn(Optional.of(mock(Product.class)));
        when(checker.isUsed(1L)).thenReturn(false);

        DeleteProductUseCaseImpl useCase = new DeleteProductUseCaseImpl(repo, checker);

        useCase.execute(1L);

        verify(repo, times(1)).delete(1L);
    }

    @Test
    void delete_throws_not_found_when_missing() {
        ProductRepository repo = mock(ProductRepository.class);
        ProductInUseChecker checker = mock(ProductInUseChecker.class);

        when(repo.findById(2L)).thenReturn(Optional.empty());

        DeleteProductUseCaseImpl useCase = new DeleteProductUseCaseImpl(repo, checker);

        assertThrows(DomainException.class, () -> useCase.execute(2L));

        verify(repo, never()).delete(anyLong());
    }

    @Test
    void delete_throws_in_use_when_referenced() {
        ProductRepository repo = mock(ProductRepository.class);
        ProductInUseChecker checker = mock(ProductInUseChecker.class);

        when(repo.findById(3L)).thenReturn(Optional.of(mock(Product.class)));
        when(checker.isUsed(3L)).thenReturn(true);

        DeleteProductUseCaseImpl useCase = new DeleteProductUseCaseImpl(repo, checker);

        assertThrows(DomainException.class, () -> useCase.execute(3L));

        verify(repo, never()).delete(3L);
    }
}
