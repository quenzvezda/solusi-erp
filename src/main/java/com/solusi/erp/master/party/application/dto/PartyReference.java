package com.solusi.erp.master.party.application.dto;

/**
 * Lightweight reference DTO for Party — safe for cross-module use (e.g. security).
 * Contains only the minimal fields needed to identify a party without exposing the full domain model.
 */
public record PartyReference(Long id, String code, String name) {}
