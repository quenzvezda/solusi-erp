package com.solusi.erp.security.role.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface RoleLookupProvider {
    LookupDto resolve(Long roleId);
}
