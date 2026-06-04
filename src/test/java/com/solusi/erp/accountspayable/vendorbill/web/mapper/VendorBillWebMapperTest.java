package com.solusi.erp.accountspayable.vendorbill.web.mapper;

import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillDetailView;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillLineView;
import com.solusi.erp.accountspayable.vendorbill.application.usecase.query.VendorBillSummaryView;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillDetailResponse;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillLineRequest;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveCommand;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveRequest;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VendorBillWebMapperTest {

    private VendorBillWebMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VendorBillWebMapper();
    }

    @Test
    void toCreateCommand_should_map_line_items_and_header_fields() {
        VendorBillSaveRequest request = new VendorBillSaveRequest();
        request.setVendorId(10L);
        request.setVendorInvoiceNumber("INV-001");
        request.setBillDate(LocalDate.of(2026, 5, 10));
        request.setDueDate(LocalDate.of(2026, 5, 20));
        request.setCurrencyId(1L);
        request.setNotes("notes");
        request.setGrIds(List.of(88L, 89L));

        VendorBillLineRequest line = new VendorBillLineRequest();
        line.setId(11L);
        line.setGrLineId(1001L);
        line.setProductId(2001L);
        line.setProductName("Product A");
        line.setDescription("Line A");
        line.setQtyBilled(new BigDecimal("2.0000"));
        line.setUomId(1L);
        line.setUomName("PCS");
        line.setUnitPrice(new BigDecimal("10.0000"));
        line.setInventoryAmount(new BigDecimal("20.0000"));
        line.setTaxAmount(new BigDecimal("2.0000"));
        request.setLines(List.of(line));

        VendorBillSaveCommand command = mapper.toCreateCommand(request);

        assertThat(command.vendorId()).isEqualTo(10L);
        assertThat(command.vendorInvoiceNumber()).isEqualTo("INV-001");
        assertThat(command.grIds()).containsExactly(88L, 89L);
        assertThat(command.lines()).hasSize(1);
        assertThat(command.lines().getFirst().grLineId()).isEqualTo(1001L);
        assertThat(command.lines().getFirst().qtyBilled()).isEqualByComparingTo("2.0000");
    }

    @Test
    void toDetailResponse_should_map_lines_and_header_fields() {
        VendorBillDetailView view = new VendorBillDetailView(
                1L,
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                1L,
                BigDecimal.ONE,
                VendorBillDocumentStatus.CONFIRMED,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                new BigDecimal("50.0000"),
                new BigDecimal("50.0000"),
                "notes",
                List.of(88L),
                List.of(new VendorBillLineView(
                        11L,
                        1001L,
                        2001L,
                        "Product A",
                        "Line A",
                        new BigDecimal("10.0000"),
                        1L,
                        "PCS",
                        new BigDecimal("10.0000"),
                        new BigDecimal("90.0000"),
                        new BigDecimal("10.0000"),
                        new BigDecimal("100.0000")
                ))
        );

        VendorBillDetailResponse response = mapper.toDetailResponse(view);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("VB-202605-00001");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().getFirst().getGrLineId()).isEqualTo(1001L);
        assertThat(response.getLines().getFirst().getLineTotal()).isEqualByComparingTo("100.0000");
    }

    @Test
    void toSummaryResponse_should_map_summary_fields() {
        VendorBillSummaryView view = new VendorBillSummaryView(
                1L,
                "VB-202605-00001",
                10L,
                "INV-001",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 20),
                VendorBillDocumentStatus.DRAFT,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        VendorBillSummaryResponse response = mapper.toSummaryResponse(view);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("VB-202605-00001");
        assertThat(response.getVendorInvoiceNumber()).isEqualTo("INV-001");
        assertThat(response.getStatus()).isEqualTo("DRAFT");
    }
}
