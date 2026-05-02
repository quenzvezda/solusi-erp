package com.solusi.erp.accounting.journal.application.usecase.command;

@FunctionalInterface
public interface PostJournalForEventUseCase {
    void execute(JournalPostingCommand command);
}
