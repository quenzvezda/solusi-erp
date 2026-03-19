# Project Workflow

## Guiding Principles

1. **The Plan is the Source of Truth:** All work must be tracked in `plan.md`
2. **The Tech Stack is Deliberate:** Changes to the tech stack must be documented in `tech-stack.md` *before* implementation
3. **Consistent Architecture:** Every feature must follow the established patterns (BaseModel, DTOs, MapStruct, Thymeleaf fragments) defined in `conductor/code_styleguides/` and the technical specifications in `docs/spec/`.
4. **User Experience First:** Every decision should prioritize user experience and consistency with existing UI.
5. **Authorized Git Operations:** The agent can execute Git commands (`git commit`, `git add`) ONLY when explicitly instructed by the user or as part of a finalized track protocol.
6. **No Server Management:** The agent MUST NOT run build or server commands (`mvn clean`, `mvn spring-boot:run`, etc.). The user handles building and rebooting the server.
7. **Manual Feedback Loop:** After implementation, the agent waits for user feedback (success, error, or bug) before proceeding.

## Task Workflow

All tasks follow a strict lifecycle:

### Standard Task Workflow

1. **Select Task:** Choose the next available task from `plan.md` in sequential order.

2. **Mark In Progress:** Before beginning work, edit `plan.md` and change the task from `[ ]` to `[~]`.

3. **Implementation:**
   - Write the minimum amount of application code necessary to fulfill the task.
   - Ensure the code follows the project's code style guidelines.
   - If bugs or compilation errors are encountered, resolve them immediately.

4. **Stage Changes:**
   - Once the task (including any immediate bug fixes) is complete, stage all relevant changes using `git add .`.
   - **DO NOT** commit at this stage. Commits are reserved for the end of the track.

5. **Provide Task Summary and Verification Guide:**
   - The agent MUST provide a detailed summary in the chat:
     - **What changed:** List of all created/modified files and a summary of the changes.
     - **How it works:** Brief explanation of the technical implementation.
     - **Manual Verification Guide:** Step-by-step instructions for the user to verify the changes manually.

6. **Wait for User Feedback:**
   - The agent MUST pause and wait for the user to manually build, run, and verify the changes.
   - Proceed ONLY after the user confirms the task is successful.

7. **Update Plan:**
   - After user confirmation, update `plan.md` by changing the task status from `[~]` to `[x]`.

### Phase Completion Verification and Checkpointing Protocol

**Trigger:** This protocol is executed immediately after a task is completed that also concludes a phase in `plan.md`.

1.  **Announce Phase Completion:** Inform the user that the phase is complete.

2.  **Summary of Phase:** Provide a high-level summary of all changes made during the phase.

3.  **Await Explicit User Feedback:**
    -   Wait for the user to confirm the entire phase is working as expected.
    -   **PAUSE** and await the user's response.

4.  **Update Plan:**
    -   Update `plan.md` to mark the phase as complete.

### Track Completion Protocol

**Trigger:** This protocol is executed after all implementation phases in the `plan.md` are complete.

1.  **Documentation Phase:**
    -   Identify all new features, architectural changes, or generic components introduced in this track.
    -   Update existing documentation in `docs/` or create new documentation files as needed.
    -   Prioritize documenting generic features that will serve as standards for future development.
    -   Stage documentation changes with `git add .`.

2.  **Final Review:**
    -   Perform a final sanity check of all staged changes.
    -   Ensure all Quality Gates are met.

3.  **Final Track Commit:**
    -   Execute a single, comprehensive commit for the entire track.
    -   The commit message MUST follow the **Commit Guidelines** and summarize the entire track's achievements.

4.  **Mark Track Complete:**
    -   Update `conductor/tracks.md` to mark the track as completed.
    -   Update the track's `metadata.json` status to `completed`.

## Quality Gates

Before marking any task complete, verify:

- [ ] Feature works as specified (User Manual Verification)
- [ ] Code follows project's code style guidelines (as defined in `code_styleguides/`)
- [ ] Documentation updated if needed
- [ ] No security vulnerabilities introduced

## Commit Guidelines

### Message Format
```
<type>(<scope>): <description>

<body>
```

### Body Requirements (Mandatory)
The body must provide technical context and follow these best practices:
- **Bullet Points:** List key technical changes (Entities, Services, Mappers, UI).
- **Logic Explanation:** Briefly explain the "how" for complex business logic.
- **Impact:** Note any database migrations, menu refactors, or cross-module impacts.
- **Verification:** Confirm that unit/integration tests were added and passed.

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Formatting, missing semicolons, etc.
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `test`: Adding missing tests
- `chore`: Maintenance tasks
- `conductor`: Conductor-specific tasks (setup, plan updates)
