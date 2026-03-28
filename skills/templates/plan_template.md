# Plan Template

Overview:
- Feature / scope:
- Goal:
- Background / context (short):

Acceptance criteria:
- List of concrete acceptance criteria (pass tests, docs updated, no breaking changes)

Test package layout:
- Tests will live under src/test/java mirroring main packages, e.g.:
  src/test/java/com/solusi/erp/inventory/<feature>/web/template/integration

Implementation steps:
1. Discover: read docs (docs/NOTES.md, docs/index.md, docs/spec) and scan templates
2. Generate per-feature todos and test skeletons
3. Pause for plan approval (human)
4. For each feature: implement tests, run mvn clean test
   - If tests fail, attempt non-breaking fix
   - If fix is breaking, stop and report
5. On success: stage and commit code (do not push)
6. Append report entry to docs/REPORT.md

Reporting:
- Use report_entry_template.md for each iteration

Notes:
- Always include Co-authored-by trailer in commits
- Do not push without explicit user consent