package com.solusi.erp.master.bankaccount.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountLookupProvider;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccount;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;

import java.util.HashMap;
import java.util.Map;

public class BankAccountLookupProviderImpl implements BankAccountLookupProvider {

    private final BankAccountJpaRepository bankAccountJpaRepository;

    public BankAccountLookupProviderImpl(BankAccountJpaRepository bankAccountJpaRepository) {
        this.bankAccountJpaRepository = bankAccountJpaRepository;
    }

    @Override
    public LookupDto resolve(Long bankAccountId) {
        if (bankAccountId == null) return null;
        return bankAccountJpaRepository.findById(bankAccountId)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(BankAccount ba) {
        String subText = ba.getCode() + " - " + ba.getBankName();
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentType", ba.getAccountType() != null ? ba.getAccountType().name() : null);
        payload.put("currencyId", ba.getCurrencyId());
        payload.put("coaId", ba.getCoaId());
        return new LookupDto(ba.getId(), ba.getAccountName(), subText, payload);
    }
}
