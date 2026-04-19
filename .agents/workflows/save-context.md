---
description: Save current session context to .jarvis/ files for cross-machine sync
---

# Save Context Workflow

This workflow persists the current session state so Bharat can resume on another machine after a `git pull`.

## Steps

1. **Review the current session** — Look at what was discussed, what tasks were worked on, and any decisions made.

2. **Update `.jarvis/tasklist.md`** — Reflect the current state of all tasks:
   - Move completed items from "In Progress" to "Recently Completed"
   - Add any new tasks identified during the session to "In Progress" or "Up Next"
   - Include enough detail for each task that Jarvis can resume without the chat history
   - Format:
     ```
     ### Task Title
     - **Status**: In Progress / Blocked / Up Next
     - **Branch**: `feature/branch-name` (if applicable)
     - **What was done**: Brief summary of progress
     - **Next steps**: Specific actions to continue
     - **Files touched**: Key files modified
     ```

3. **Update `.jarvis/context.md`** — Capture the working context:
   - **Last Session**: Set date, machine (if known), and a 1-2 sentence summary
   - **Current Focus**: What feature/area Bharat is actively working on
   - **Key Decisions & Notes**: Any architectural decisions, trade-offs discussed, or important context
   - **Blockers & Open Questions**: Anything unresolved that needs attention

4. **Update `.jarvis/changelog.md`** — Append a brief entry at the top under today's date:
   - One bullet per meaningful action completed
   - Keep it concise — this is a log, not documentation

5. **Remind Bharat** to commit and push the `.jarvis/` changes:
   ```
   git add .jarvis/ && git commit -m "jarvis: save session context" && git push
   ```
