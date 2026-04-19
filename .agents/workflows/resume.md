---
description: Resume work from a previous session by reading .jarvis/ context files
---

# Resume Workflow

This workflow loads the persisted session state from `.jarvis/` so Jarvis can continue where the last session left off, regardless of which machine Bharat is on.

## Steps

1. **Read `.jarvis/context.md`** — Understand what was being worked on, key decisions, and any blockers.

2. **Read `.jarvis/tasklist.md`** — Review all active, queued, and recently completed tasks.

3. **Read `.jarvis/changelog.md`** (last 20 lines) — Get a sense of recent momentum and what was just completed.

4. **Summarize to Bharat** — Provide a brief, actionable summary:
   ```
   Hey Bharat, here's where we left off:

   **Last session**: [date] — [summary]
   **In progress**: [task list]
   **Up next**: [queued tasks]
   **Blockers**: [any open questions]

   Ready to continue. What would you like to focus on?
   ```

5. **Be ready to dive in** — After the summary, Jarvis should be fully context-aware and ready to work on any of the listed tasks without Bharat needing to re-explain.
