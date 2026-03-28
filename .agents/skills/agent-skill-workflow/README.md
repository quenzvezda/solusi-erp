Skills: agent-skill-workflow

This folder contains a reusable, generic agentic skill for discover→plan→implement→test→fix→commit workflows. It is configurable via inputs (run_id suffix, relative repo_root, artifact_dir).

How to use:
- Run discover to collect context into docs/NOTES-{run_id}.md.
- Generate plan: creates docs/PLAN-{run_id}.md using templates.
- Human reviews and approves plan (set allow_commit_docs and consent).
- Implementer executes plan; tester runs tests; fixer attempts non-breaking fixes.
- Committer creates commits (no push) with English Conventional Commit messages.

Files created:
- agent-skill-workflow.yaml (manifest)
- templates/plan_template.md
- templates/report_entry_template.md
- prompts/ (discover, plan_generator, implementer, tester, fixer, committer)

Notes:
- Defaults use relative paths; do not hardcode repo_root.
- Committer defaults to not appending Co-authored-by trailer.
