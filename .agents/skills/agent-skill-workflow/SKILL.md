---
name: agent-skill-workflow
description: Generic agentic skill for discover→plan→implement→test→fix→commit workflows. Use when asked to orchestrate or automate test-and-fix workflows for features, with artifacts saved per run_id.
license: MIT
---

This skill provides instructions, prompts and helper scripts to run an end-to-end workflow:

- discover: collect context and save NOTES-{run_id}.md
- plan: generate PLAN-{run_id}.md
- implement: create test skeletons and run tests according to `test_scope`
- test: run targeted or full Maven tests and capture artifacts
- fixer: attempt safe, non-breaking fixes and save patches
- committer: create commits (no push) following Conventional Commits and optional Co-authored-by trailer

Files included in this skill directory:
- agent-skill-workflow.yaml (manifest copy)
- prompts/ (discover, plan_generator, implementer, tester, fixer, committer)
- templates/ (plan_template.md, report_entry_template.md)
- run_skill_workflow.ps1 (Windows runner)
- run_skill_workflow.sh (POSIX runner)

Usage examples (in Copilot CLI):
- "Use the /agent-skill-workflow skill to generate a plan for inventory form tests with run_id 4538"

Local script usage (manual):
- PowerShell (Windows):
  powershell -File .\\.agents\\skills\\agent-skill-workflow\\run_skill_workflow.ps1 -RunId 4538 -TestScope changed -ArtifactDir docs\\reports -MavenCmd mvn
- Bash (Linux/macOS):
  ./.agents/skills/agent-skill-workflow/run_skill_workflow.sh 4538 changed docs/reports mvn

Notes for Copilot integration:
- SKILL.md must exist for Copilot to discover the skill. The file contains YAML frontmatter (name, description) followed by the instructions Copilot will inject into context when using this skill.
- If you add or modify skills during a running Copilot session, run `/skills reload` in the CLI or restart Copilot so the CLI rescans available skills.

Safety:
- This skill recommends human checkpoints (plan_approved, approve_breaking_fix) and will not push commits without explicit consent.

