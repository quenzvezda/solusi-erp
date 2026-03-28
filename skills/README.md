Skills: inventory-form-tests

This folder contains a reusable agentic skill to run the inventory form test workflow.

How to use:
- Invoke discover prompt to build context bundle.
- Invoke plan_generator to produce Plan.md draft.
- Human reviews Plan.md and sets allow_commit_docs flag.
- Invoke implementer to execute the plan, tester to run tests, fixer to attempt safe fixes, committer to commit changes.

Files created:
- agent-skill-inventory-form-tests.yaml (manifest)
- templates/plan_template.md
- templates/report_entry_template.md
- prompts/ (discover, plan_generator, implementer, tester, fixer, committer)

Notes:
- Skill is designed for human-in-the-loop workflows and will always pause for plan approval and before pushing.
