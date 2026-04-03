package com.solusi.erp.core.infrastructure.sequence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic Template-Based Sequence Generator.
 */
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final SystemSequenceRepository repository;
    private static final Pattern DATE_PATTERN = Pattern.compile("\\{date:([^}]+)\\}");

    /**
     * Generates a new code based on the module's template and increments current value.
     * Uses Propagation.REQUIRES_NEW to ensure the sequence is committed even if the main transaction fails.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generate(String moduleCode) {
        SystemSequence seq = repository.findById(moduleCode)
                .orElseThrow(() -> new RuntimeException("Sequence configuration not found for module: " + moduleCode));

        checkAndReset(seq);

        seq.setCurrentValue(seq.getCurrentValue() + 1);
        repository.save(seq);

        return parseTemplate(seq);
    }

    private void checkAndReset(SystemSequence seq) {
        LocalDateTime now = LocalDateTime.now();
        if (seq.getLastResetDate() == null) {
            seq.setLastResetDate(now);
            return;
        }

        boolean shouldReset = false;
        switch (seq.getResetCycle()) {
            case DAILY -> shouldReset = now.getDayOfYear() != seq.getLastResetDate().getDayOfYear() 
                                    || now.getYear() != seq.getLastResetDate().getYear();
            case MONTHLY -> shouldReset = now.getMonthValue() != seq.getLastResetDate().getMonthValue() 
                                      || now.getYear() != seq.getLastResetDate().getYear();
            case YEARLY -> shouldReset = now.getYear() != seq.getLastResetDate().getYear();
            case NEVER -> shouldReset = false;
        }

        if (shouldReset) {
            seq.setCurrentValue(0L);
            seq.setLastResetDate(now);
        }
    }

    private String parseTemplate(SystemSequence seq) {
        String template = seq.getFormatPattern();
        LocalDateTime now = LocalDateTime.now();

        // 1. Handle Date Placeholders {date:FORMAT}
        Matcher matcher = DATE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String format = matcher.group(1);
            matcher.appendReplacement(sb, now.format(DateTimeFormatter.ofPattern(format)));
        }
        matcher.appendTail(sb);
        template = sb.toString();

        // 2. Handle Sequence Placeholder {seq}
        String formattedSeq = String.format("%0" + seq.getPadLength() + "d", seq.getCurrentValue());
        template = template.replace("{seq}", formattedSeq);

        return template;
    }
}
