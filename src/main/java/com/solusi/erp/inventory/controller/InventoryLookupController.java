package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.service.ContainerService;
import com.solusi.erp.inventory.service.FacilityService;
import com.solusi.erp.inventory.service.GridService;
import com.solusi.erp.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lookup/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('LOOKUP_INVENTORY')")
public class InventoryLookupController {

    private final ProductService productService;
    private final FacilityService facilityService;
    private final GridService gridService;
    private final ContainerService containerService;

    @GetMapping("/products/{id}")
    public LookupDto getLookupProduct(@PathVariable Long id) {
        return productService.getLookupProduct(id);
    }

    @GetMapping("/facilities/{id}")
    public LookupDto getLookupFacility(@PathVariable Long id) {
        return facilityService.getLookupFacility(id);
    }

    @GetMapping("/grids/{id}")
    public LookupDto getLookupGrid(@PathVariable Long id) {
        return gridService.getLookupGrid(id);
    }

    @GetMapping("/containers/{id}")
    public LookupDto getLookupContainer(@PathVariable Long id) {
        return containerService.getLookupContainer(id);
    }

    @GetMapping("/products")
    public List<LookupDto> lookupProducts(@RequestParam(value = "q", defaultValue = "") String q,
                                         @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return productService.lookupProducts(q, limit);
    }

    @GetMapping("/facilities")
    public List<LookupDto> lookupFacilities(@RequestParam(value = "q", defaultValue = "") String q,
                                           @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return facilityService.lookupFacilities(q, limit);
    }

    @GetMapping("/grids")
    public List<LookupDto> lookupGrids(@RequestParam(value = "q", defaultValue = "") String q,
                                      @RequestParam(value = "facilityId", required = false) Long facilityId,
                                      @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return gridService.lookupGrids(q, facilityId, limit);
    }

    @GetMapping("/containers")
    public List<LookupDto> lookupContainers(@RequestParam(value = "q", defaultValue = "") String q,
                                           @RequestParam(value = "facilityId", required = false) Long facilityId,
                                           @RequestParam(value = "gridId", required = false) Long gridId,
                                           @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return containerService.lookupContainers(q, facilityId, gridId, limit);
    }
}
