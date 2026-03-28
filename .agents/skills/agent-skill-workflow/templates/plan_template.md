# Plan Template

Overview:
- Feature / scope:
- Goal:
- Background / context (short):

Acceptance criteria:
- List of concrete acceptance criteria (pass tests, docs updated, no breaking changes)

Test package layout:
- Tests will live under src/test/java mirroring main packages, e.g.:
  src/test/java/com/solusi/erp/<module>/<feature>/...

Implementation steps:
1. Discover: read docs/NOTES-{run_id}.md, docs/index.md, docs/spec and scan templates
2. Generate per-feature todos and test skeletons
3. Pause for plan approval (human)
4. For each feature: implement tests, run mvn clean test (or follow `test_scope` directive: full|changed|module|smoke).
   - If tests fail, attempt non-breaking fix
   - If fix is breaking, stop and report
5. On success: stage and commit code (do not push). Commit messages must be in English and follow Conventional Commits.
6. Append report entry to {artifact_dir}/REPORT-{run_id}.md and save artifacts under {artifact_dir}/{run_id}/

Reporting:
- Use report_entry_template.md for each iteration

Notes:
- Do NOT include Co-authored-by trailer in commits unless `append_coauth_trailer` is explicitly enabled by user consent.
- Do not push without explicit user consent