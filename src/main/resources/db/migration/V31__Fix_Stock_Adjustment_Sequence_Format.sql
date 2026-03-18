UPDATE system_sequences 
SET format_pattern = 'ADJ-{date:yyMM}-{seq}'
WHERE module_code = 'STOCK_ADJUSTMENT';
