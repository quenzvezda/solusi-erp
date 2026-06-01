package com.solusi.erp.inventory.goodsissue.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsIssueQueryUseCaseTest {

    @Mock
    private GoodsIssueRepository goodsIssueRepository;

    private GetGoodsIssueUseCase getUseCase;
    private GetGoodsIssueEditViewUseCase editViewUseCase;
    private FindGoodsIssuesUseCase findUseCase;

    @BeforeEach
    void setUp() {
        getUseCase = new GetGoodsIssueUseCaseImpl(goodsIssueRepository);
        editViewUseCase = new GetGoodsIssueEditViewUseCaseImpl(goodsIssueRepository);
        findUseCase = new FindGoodsIssuesUseCaseImpl(goodsIssueRepository);
    }

    @Test
    void getUseCase_delegatesToRepository() {
        GoodsIssue issue = org.mockito.Mockito.mock(GoodsIssue.class);
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));

        Optional<GoodsIssue> result = getUseCase.execute(7L);

        assertThat(result).contains(issue);
        verify(goodsIssueRepository).findById(7L);
    }

    @Test
    void editViewUseCase_delegatesToRepository() {
        GoodsIssue issue = org.mockito.Mockito.mock(GoodsIssue.class);
        when(goodsIssueRepository.findById(7L)).thenReturn(Optional.of(issue));

        Optional<GoodsIssue> result = editViewUseCase.execute(7L);

        assertThat(result).contains(issue);
        verify(goodsIssueRepository).findById(7L);
    }

    @Test
    void findUseCase_delegatesToRepository() {
        Pageable pageable = Pageable.of(0, 10, "issueDate", "desc");
        Page<GoodsIssue> expected = new Page<>(List.of(), 0, 10, 0);
        when(goodsIssueRepository.findAll("PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable))
                .thenReturn(expected);

        Page<GoodsIssue> result = findUseCase.execute(
                "PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable);

        assertThat(result).isSameAs(expected);
        verify(goodsIssueRepository).findAll("PRTN-0007", GoodsIssueReferenceType.PURCHASE_RETURN, 7L, pageable);
    }
}
