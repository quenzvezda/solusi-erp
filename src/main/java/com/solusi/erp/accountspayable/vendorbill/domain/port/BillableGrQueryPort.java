package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BillableGrQueryPort {
    List<BillableGrView> findBillableGrs(Long vendorId, Long currencyId);

    List<BillableGrLineView> findBillableGrLines(Long grId);

    Map<Long, BigDecimal> sumConfirmedBilledQtyByGrId(Long grId);

    GrLineData getGrLineData(Long grLineId);

    BigDecimal sumConfirmedLineTotals(Long grLineId, Long excludeBillId);

    BigDecimal sumConfirmedBilledQty(Long grLineId, Long excludeBillId);

    record GrLineData(BigDecimal quantityReceived, BigDecimal grIrAmount) {}
}
