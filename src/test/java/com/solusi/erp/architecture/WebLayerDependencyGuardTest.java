package com.solusi.erp.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WebLayerDependencyGuardTest {

    private static final Path MAIN_JAVA = Path.of("src", "main", "java");
    private static final String GOODS_RECEIPT_WEB_MAPPER =
            "com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java";
    private static final Set<String> BASELINE_VIOLATIONS = Set.of(
            "com/solusi/erp/common/approval/web/controller/ApprovalController.java declares repository field ApprovalRequestRepository",
            "com/solusi/erp/common/approval/web/controller/ApprovalController.java imports repository type ApprovalRequestRepository",
            "com/solusi/erp/common/news/web/controller/NewsController.java declares repository field NewsRepository",
            "com/solusi/erp/common/news/web/controller/NewsController.java imports repository type NewsRepository",
            "com/solusi/erp/core/web/controller/DashboardController.java declares repository field ApprovalRequestRepository",
            "com/solusi/erp/core/web/controller/DashboardController.java declares repository field NewsRepository",
            "com/solusi/erp/core/web/controller/DashboardController.java imports repository type ApprovalRequestRepository",
            "com/solusi/erp/core/web/controller/DashboardController.java imports repository type NewsRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java declares repository field BrandJpaRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java declares repository field ProductCategoryJpaRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java declares repository field UomJpaRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java imports repository type BrandJpaRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java imports repository type ProductCategoryJpaRepository",
            "com/solusi/erp/inventory/product/web/mapper/ProductWebMapper.java imports repository type UomJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java declares repository field ContainerJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java declares repository field FacilityJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java declares repository field GridJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java declares repository field JpaProductRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java imports repository type ContainerJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java imports repository type FacilityJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java imports repository type GridJpaRepository",
            "com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java imports repository type JpaProductRepository",
            "com/solusi/erp/master/currency/web/controller/CurrencyLookupController.java declares repository field CurrencyJpaRepository",
            "com/solusi/erp/master/currency/web/controller/CurrencyLookupController.java imports repository type CurrencyJpaRepository",
            "com/solusi/erp/master/party/web/controller/PartyController.java declares repository field PartyIdentificationTypeJpaRepository",
            "com/solusi/erp/master/party/web/controller/PartyController.java declares repository field PartyRoleTypeJpaRepository",
            "com/solusi/erp/master/party/web/controller/PartyController.java imports repository type PartyIdentificationTypeJpaRepository",
            "com/solusi/erp/master/party/web/controller/PartyController.java imports repository type PartyRoleTypeJpaRepository",
            "com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java declares repository field PurchaseRequisitionRepository",
            "com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java imports repository type PurchaseRequisitionRepository",
            "com/solusi/erp/purchasing/purchaserequisition/web/controller/PurchaseRequisitionController.java declares repository field CurrencyRepository",
            "com/solusi/erp/purchasing/purchaserequisition/web/controller/PurchaseRequisitionController.java imports repository type CurrencyRepository",
            "com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListLookupController.java declares repository field SupplierPriceListRepository",
            "com/solusi/erp/purchasing/supplierpricelist/web/controller/SupplierPriceListLookupController.java imports repository type SupplierPriceListRepository",
            "com/solusi/erp/security/user/web/controller/PasswordResetController.java declares repository field UserJpaRepository"
    );
    private static final Pattern REPOSITORY_IMPORT_PATTERN = Pattern.compile(
            "(?m)^\\s*import\\s+([\\w$.]*\\.([A-Z]\\w*Repository))\\s*;"
    );
    private static final Pattern REPOSITORY_FIELD_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:(?:private|protected|public)\\s+)?(?:final\\s+)?(?:[\\w$.]+\\.)?([A-Z]\\w*Repository)\\s+\\w+\\s*(?:;|=|,)"
    );

    @Test
    @DisplayName("web controller and mapper classes must not import or declare repository dependencies")
    void webControllerAndMapperClassesMustNotDependOnRepositories() throws IOException {
        Set<String> violations = scanViolations();

        assertThat(violations)
                .as("GoodsReceiptWebMapper must stay repository-free")
                .noneMatch(violation -> violation.startsWith(GOODS_RECEIPT_WEB_MAPPER));

        Set<String> unexpectedViolations = new TreeSet<>(violations);
        unexpectedViolations.removeAll(BASELINE_VIOLATIONS);

        assertThat(unexpectedViolations)
                .as("New repository dependencies found in web layer:\n%s", String.join("\n", unexpectedViolations))
                .isEmpty();

        Set<String> resolvedBaselineEntries = new TreeSet<>(BASELINE_VIOLATIONS);
        resolvedBaselineEntries.removeAll(violations);

        assertThat(resolvedBaselineEntries)
                .as("Baseline entries can be removed:\n%s", String.join("\n", resolvedBaselineEntries))
                .isEmpty();
    }

    private boolean isGuardedWebControllerOrMapper(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.contains("/web/controller/") || normalized.contains("/web/mapper/");
    }

    private Set<String> scanViolations() throws IOException {
        try (Stream<Path> paths = Files.walk(MAIN_JAVA)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isGuardedWebControllerOrMapper)
                    .flatMap(this::findViolations)
                    .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        }
    }

    private Stream<String> findViolations(Path path) {
        try {
            String sanitizedSource = stripComments(Files.readString(path));
            String relativePath = MAIN_JAVA.relativize(path).toString().replace('\\', '/');

            Stream<String> importViolations = matchViolations(
                    sanitizedSource,
                    REPOSITORY_IMPORT_PATTERN,
                    relativePath,
                    matcher -> "imports repository type " + matcher.group(2)
            );

            Stream<String> fieldViolations = matchViolations(
                    sanitizedSource,
                    REPOSITORY_FIELD_PATTERN,
                    relativePath,
                    matcher -> "declares repository field " + matcher.group(1)
            );

            return Stream.concat(importViolations, fieldViolations);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect " + path, e);
        }
    }

    private Stream<String> matchViolations(String source,
                                           Pattern pattern,
                                           String relativePath,
                                           java.util.function.Function<MatchResult, String> messageFactory) {
        return pattern.matcher(source).results()
                .map(messageFactory)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(message -> relativePath + " " + message);
    }

    private String stripComments(String source) {
        String withoutBlockComments = source.replaceAll("(?s)/\\*.*?\\*/", "");
        return withoutBlockComments.replaceAll("(?m)//.*$", "");
    }
}
