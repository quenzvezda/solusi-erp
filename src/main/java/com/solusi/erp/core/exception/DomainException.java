package com.solusi.erp.core.exception;

import lombok.Getter;

/**
 * Exception base class for Domain logic errors.
 * It carries an i18n message key and optional arguments,
 * keeping the Domain Model free from Spring/Infrastructure dependencies.
 */
@Getter
public class DomainException extends RuntimeException {
    
    private final String key;
    private final Object[] args;

    public DomainException(String key, Object... args) {
        super(key); // Default exception message is the key itself
        this.key = key;
        this.args = args;
    }
}
