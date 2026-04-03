package com.solusi.erp.security.menusearch.application.usecase;

import com.solusi.erp.security.menusearch.application.port.MenuQueryPort;
import com.solusi.erp.security.menusearch.web.dto.MenuNodeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuildMenuTreeUseCase Tests")
class BuildMenuTreeUseCaseImplTest {

    @Mock
    private MenuQueryPort menuQueryPort;

    private BuildMenuTreeUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new BuildMenuTreeUseCaseImpl(menuQueryPort);
    }

    @Test
    @DisplayName("returns empty list when no menu items found")
    void execute_returnsEmptyList_whenNoItems() {
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_USER"))).thenReturn(List.of());

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_USER"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("builds single-level node from single-part breadcrumb")
    void execute_buildsSingleNode_forSinglePartBreadcrumb() {
        MenuQueryPort.MenuItem item = new MenuQueryPort.MenuItem(
                "/master/tax", "Tax", "Tax",
                "ti-receipt-tax", "Tax", "Tax"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(item));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        assertThat(result).hasSize(1);
        MenuNodeResponse node = result.get(0);
        assertThat(node.getNameId()).isEqualTo("Tax");
        assertThat(node.getUrlPath()).isEqualTo("/master/tax");
        assertThat(node.getIconClass()).isEqualTo("ti-receipt-tax");
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("builds two-level tree from two-part breadcrumb")
    void execute_buildsTwoLevelTree() {
        MenuQueryPort.MenuItem item = new MenuQueryPort.MenuItem(
                "/master/tax", "Tax", "Master Data > Tax",
                "ti-receipt-tax", "Master Data > Tax", "Master Data > Tax"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(item));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        assertThat(result).hasSize(1);
        MenuNodeResponse parent = result.get(0);
        assertThat(parent.getNameId()).isEqualTo("Master Data");
        assertThat(parent.getUrlPath()).isNull();
        assertThat(parent.getIconClass()).isEqualTo("ti-database"); // from PARENT_ICONS
        assertThat(parent.getChildren()).hasSize(1);

        MenuNodeResponse child = parent.getChildren().get(0);
        assertThat(child.getNameId()).isEqualTo("Tax");
        assertThat(child.getUrlPath()).isEqualTo("/master/tax");
        assertThat(child.getIconClass()).isEqualTo("ti-receipt-tax");
    }

    @Test
    @DisplayName("merges sibling items under the same parent node")
    void execute_mergesSiblings_underSameParent() {
        MenuQueryPort.MenuItem tax = new MenuQueryPort.MenuItem(
                "/master/tax", "Tax", "Master Data > Tax",
                "ti-receipt-tax", "Master Data > Tax", "Master Data > Tax"
        );
        MenuQueryPort.MenuItem currency = new MenuQueryPort.MenuItem(
                "/master/currency", "Currency", "Master Data > Currency",
                "ti-currency-dollar", "Master Data > Currency", "Master Data > Currency"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(tax, currency));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        assertThat(result).hasSize(1);
        MenuNodeResponse parent = result.get(0);
        assertThat(parent.getNameId()).isEqualTo("Master Data");
        assertThat(parent.getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("updates urlPath and iconClass when leaf node already exists in tree")
    void execute_updatesExistingLeafNode() {
        MenuQueryPort.MenuItem first = new MenuQueryPort.MenuItem(
                "/old/path", "Menu", "Master Data > Item",
                "ti-old", "Master Data > Item", "Master Data > Item"
        );
        MenuQueryPort.MenuItem second = new MenuQueryPort.MenuItem(
                "/new/path", "Menu", "Master Data > Item",
                "ti-new", "Master Data > Item", "Master Data > Item"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(first, second));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        MenuNodeResponse leaf = result.get(0).getChildren().get(0);
        assertThat(leaf.getUrlPath()).isEqualTo("/new/path");
        assertThat(leaf.getIconClass()).isEqualTo("ti-new");
    }

    @Test
    @DisplayName("uses fallback folder icon for unknown parent names")
    void execute_usesFallbackIcon_forUnknownParent() {
        MenuQueryPort.MenuItem item = new MenuQueryPort.MenuItem(
                "/custom/path", "Custom", "Unknown Module > Feature",
                "ti-star", "Unknown Module > Feature", "Unknown Module > Feature"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(item));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        MenuNodeResponse parent = result.get(0);
        assertThat(parent.getIconClass()).isEqualTo("ti-folder");
    }

    @Test
    @DisplayName("builds three-level tree from three-part breadcrumb")
    void execute_buildsThreeLevelTree() {
        MenuQueryPort.MenuItem item = new MenuQueryPort.MenuItem(
                "/inv/adj/list", "Stock Adjustment", "Inventory > Inventory Transactions > Stock Adjustment",
                "ti-adjustments-alt",
                "Inventory > Inventory Transactions > Stock Adjustment",
                "Inventory > Inventory Transactions > Stock Adjustment"
        );
        when(menuQueryPort.findAllByAuthorities(List.of("ROLE_ADMIN"))).thenReturn(List.of(item));

        List<MenuNodeResponse> result = useCase.execute(List.of("ROLE_ADMIN"));

        assertThat(result).hasSize(1);
        MenuNodeResponse level1 = result.get(0);
        assertThat(level1.getNameId()).isEqualTo("Inventory");
        assertThat(level1.getChildren()).hasSize(1);

        MenuNodeResponse level2 = level1.getChildren().get(0);
        assertThat(level2.getNameId()).isEqualTo("Inventory Transactions");
        assertThat(level2.getChildren()).hasSize(1);

        MenuNodeResponse level3 = level2.getChildren().get(0);
        assertThat(level3.getNameId()).isEqualTo("Stock Adjustment");
        assertThat(level3.getUrlPath()).isEqualTo("/inv/adj/list");
    }
}
