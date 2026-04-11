import sys
plan_path = r'F:\solusi-program-erp\docs\plans\2026-04-10-accounting-foundation.md'
with open(plan_path, 'a', encoding='utf-8') as f:
    f.write(sys.stdin.read())
print('OK')
