package com.solusi.erp.master.geographic.domain.port;

public interface GeographicInUseChecker {
    boolean isInUse(Long geographicId);
}
