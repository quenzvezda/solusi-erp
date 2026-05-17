package com.solusi.erp.inventory.container.domain.port;

import com.solusi.erp.core.dto.LookupDto;

/**
 * Port for resolving Container data as LookupDto.
 * Single source of truth for container autocomplete representation (name + subText).
 */
public interface ContainerLookupProvider {
    LookupDto resolve(Long containerId);
}
