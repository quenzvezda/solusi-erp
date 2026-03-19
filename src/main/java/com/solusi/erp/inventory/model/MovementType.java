package com.solusi.erp.inventory.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Movement Type Enum for Stock Transactions.
 */
@Getter
@RequiredArgsConstructor
public enum MovementType {
    RECEIPT("enum.movement.type.receipt"),
    ISSUE("enum.movement.type.issue"),
    ISSUE_RESERVED("enum.movement.type.issue_reserved"),
    RESERVE("enum.movement.type.reserve"),
    RELEASE("enum.movement.type.release"),
    TRANSFER_OUT("enum.movement.type.transfer_out"),
    TRANSFER_IN("enum.movement.type.transfer_in"),
    ADJUSTMENT("enum.movement.type.adjustment");

    private final String messageKey;
}
