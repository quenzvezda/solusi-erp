# Plan: Workflow Update - Staging and Documentation

Update the Conductor workflow to prioritize staging changes over committing during the implementation phases, and ensure a mandatory documentation phase is included at the end of every track.

## Changes

### 1. Update `conductor/workflow.md`
- **Modify Standard Task Workflow:** Replace "Commit Changes" with "Stage Changes" using `git add .`.
- **Refine Bug/Fix Protocol:** Ensure fixes for bugs or compile errors are also staged rather than committed.
- **Add Track Completion Protocol:** Create a new section for completing a track, which includes:
    - A mandatory documentation phase.
    - A final comprehensive commit for the entire track.
- **Update Commit Guidelines:** Clarify that the commit message should summarize the entire track's work.

### 2. Update Plan Generation Logic (Internal)
- Ensure that future `plan.md` files generated for new tracks include a final "Documentation & Finalization" phase.

## Verification
- Review the updated `conductor/workflow.md` for clarity and adherence to the new requirements.
- Generate a test track (mentally or as a draft) to verify the new phase structure.
