package com.solusi.erp.inventory.stock.domain;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationRequest;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservationStatus;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryReservationTest {

    @Test
    void createActive_nonSerialQuantityGreaterThanOne_succeeds() {
        InventoryReservation reservation = createActive(request(false, null, BigDecimal.TEN));

        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.ACTIVE);
        assertThat(reservation.getQuantity()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(reservation.getValuationReferenceType()).isEqualTo(ReferenceType.GOODS_RECEIPT);
    }

    @Test
    void release_activeReservation_transitionsToReleased() {
        InventoryReservation reservation = createActive(request(false, null, BigDecimal.TEN));

        reservation.release();

        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.RELEASED);
    }

    @Test
    void consume_activeReservation_transitionsToConsumed() {
        InventoryReservation reservation = createActive(request(false, null, BigDecimal.TEN));

        reservation.consume();

        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.CONSUMED);
    }

    @Test
    void transition_terminalReservation_fails() {
        InventoryReservation reservation = createActive(request(false, null, BigDecimal.TEN));
        reservation.release();

        assertThatThrownBy(reservation::consume)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.not_active");
    }

    @Test
    void createActive_serializedQuantityOne_succeeds() {
        InventoryReservation reservation = createActive(request(true, "SER-001", BigDecimal.ONE));

        assertThat(reservation.getSerialNumber()).isEqualTo("SER-001");
    }

    @Test
    void createActive_serializedWithoutSerial_fails() {
        assertThatThrownBy(() -> createActive(request(true, null, BigDecimal.ONE)))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.serial_required");
    }

    @Test
    void createActive_serializedQuantityOtherThanOne_fails() {
        assertThatThrownBy(() -> createActive(request(true, "SER-001", BigDecimal.valueOf(2))))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.serial_quantity_one");
    }

    @Test
    void createActive_nonPositiveQuantity_fails() {
        assertThatThrownBy(() -> createActive(request(false, null, BigDecimal.ZERO)))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.quantity_positive");
    }

    @Test
    void createActive_withoutValuationReference_fails() {
        InventoryReservationRequest request = new InventoryReservationRequest(
                10L, 20L, 30L, 40L, false, null, null, null, null, BigDecimal.ONE);

        assertThatThrownBy(() -> createActive(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.inventory.reservation.valuation_reference_required");
    }

    private InventoryReservation createActive(InventoryReservationRequest request) {
        return InventoryReservation.createActive(ReservationOwnerType.PURCHASE_RETURN, 100L, "PRT-001", request);
    }

    private InventoryReservationRequest request(boolean serialized, String serialNumber, BigDecimal quantity) {
        return new InventoryReservationRequest(
                10L,
                20L,
                30L,
                40L,
                serialized,
                serialNumber,
                ReferenceType.GOODS_RECEIPT,
                50L,
                60L,
                quantity
        );
    }
}
