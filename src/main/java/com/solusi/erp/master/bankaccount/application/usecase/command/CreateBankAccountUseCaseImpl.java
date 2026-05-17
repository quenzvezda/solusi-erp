package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import com.solusi.erp.master.shared.model.PaymentType;

public class CreateBankAccountUseCaseImpl implements CreateBankAccountUseCase {

    private final BankAccountRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateBankAccountUseCaseImpl(BankAccountRepository repository,
                                        SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public BankAccount execute(String bankName, String branch, String accountName, String accountNo,
                               PaymentType accountType, String note, Long cityId, Long partyId,
                               Boolean isActive, Long currencyId, Long coaId) {
        String code = sequenceGeneratorService.generate("BANK_ACCOUNT");
        BankAccount bankAccount = BankAccount.createNew(code, bankName, branch, accountName,
                accountNo, accountType, note, cityId, partyId, isActive, currencyId, coaId);
        return repository.save(bankAccount);
    }
}
