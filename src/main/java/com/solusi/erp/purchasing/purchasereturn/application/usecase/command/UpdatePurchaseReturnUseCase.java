package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;

import java.time.LocalDate;
import java.util.List;

public interface UpdatePurchaseReturnUseCase {

    PurchaseReturn execute(Long id, LocalDate returnDate, PurchaseReturnReason reason,
                           String note, List<PurchaseReturnLineCommand> lines);
}
