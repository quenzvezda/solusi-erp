package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;
import com.solusi.erp.security.menusearch.web.dto.MenuNodeResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BuildMenuTreeUseCaseImpl implements BuildMenuTreeUseCase {

    private static final Map<String, String> PARENT_ICONS = Map.ofEntries(
            Map.entry("Company Admin", "ti-building-skyscraper"),
            Map.entry("Security", "ti-shield-lock"),
            Map.entry("Master Data", "ti-database"),
            Map.entry("Finance Settings", "ti-receipt-tax"),
            Map.entry("Operations", "ti-briefcase"),
            Map.entry("Inventory", "ti-packages"),
            Map.entry("Inventory Management", "ti-packages"),
            Map.entry("Inventory Setup", "ti-settings"),
            Map.entry("Inventory Transactions", "ti-transfer-in"),
            Map.entry("Inventory Reports", "ti-report-analytics"),
            Map.entry("Sales & Distribution", "ti-truck-delivery"),
            Map.entry("Procurement (Purchase)", "ti-shopping-cart"),
            Map.entry("Finance & Accounting", "ti-calculator"),
            Map.entry("Account Receivable (Sales Billing)", "ti-file-invoice"),
            Map.entry("Account Payable (Purchase Invoice)", "ti-receipt"),
            Map.entry("General Ledger", "ti-book"),
            Map.entry("Adjustments", "ti-adjustments-alt"),
            Map.entry("Production", "ti-assembly")
    );

    private final MenuQueryPort menuQueryPort;

    public BuildMenuTreeUseCaseImpl(MenuQueryPort menuQueryPort) {
        this.menuQueryPort = menuQueryPort;
    }

    @Override
    public List<MenuNodeResponse> execute(Collection<String> authorities) {
        List<MenuQueryPort.MenuItem> items = menuQueryPort.findAllByAuthorities(authorities);
        List<MenuNodeResponse> root = new ArrayList<>();

        for (MenuQueryPort.MenuItem item : items) {
            String[] partsId = item.breadcrumbId().split(" > ");
            String[] partsEn = item.breadcrumbEn().split(" > ");

            List<MenuNodeResponse> currentList = root;

            for (int i = 0; i < partsId.length; i++) {
                String partId = partsId[i];
                String partEn = (i < partsEn.length) ? partsEn[i] : partId;
                boolean isLast = (i == partsId.length - 1);

                MenuNodeResponse existing = currentList.stream()
                        .filter(n -> partId.equals(n.getNameId()))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    existing = MenuNodeResponse.builder()
                            .nameId(partId)
                            .nameEn(partEn)
                            .urlPath(isLast ? item.urlPath() : null)
                            .iconClass(isLast ? item.iconClass() : PARENT_ICONS.getOrDefault(partEn, "ti-folder"))
                            .children(new ArrayList<>())
                            .build();
                    currentList.add(existing);
                } else if (isLast) {
                    existing.setUrlPath(item.urlPath());
                    existing.setIconClass(item.iconClass());
                }

                currentList = existing.getChildren();
            }
        }
        return root;
    }
}
