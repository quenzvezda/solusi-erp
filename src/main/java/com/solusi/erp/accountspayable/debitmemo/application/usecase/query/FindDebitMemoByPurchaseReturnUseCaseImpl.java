package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;

import java.util.Optional;

public class FindDebitMemoByPurchaseReturnUseCaseImpl implements FindDebitMemoByPurchaseReturnUseCase {

    private final DebitMemoRepository debitMemoRepository;

    public FindDebitMemoByPurchaseReturnUseCaseImpl(DebitMemoRepository debitMemoRepository) {
        this.debitMemoRepository = debitMemoRepository;
    }

    @Override
    public Optional<DebitMemoSourceLinkView> execute(Long purchaseReturnId) {
        if (purchaseReturnId == null) {
            return Optional.empty();
        }
        return debitMemoRepository.findByPurchaseReturnId(purchaseReturnId)
                .map(this::toLink);
    }

    private DebitMemoSourceLinkView toLink(DebitMemo debitMemo) {
        return new DebitMemoSourceLinkView(
                debitMemo.getId(),
                debitMemo.getCode(),
                debitMemo.getSettlementStatus()
        );
    }
}

