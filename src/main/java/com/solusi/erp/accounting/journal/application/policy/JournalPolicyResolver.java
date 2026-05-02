package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JournalPolicyResolver {

    private final Map<SchemaEventType, JournalPolicy> policies;

    public JournalPolicyResolver(List<JournalPolicy> policies) {
        this.policies = policies.stream()
                .collect(Collectors.toMap(JournalPolicy::supports, Function.identity()));
    }

    public JournalPolicy resolve(SchemaEventType eventType) {
        JournalPolicy policy = policies.get(eventType);
        if (policy == null) {
            throw new DomainException("msg.error.journal.policy.notfound");
        }
        return policy;
    }
}
