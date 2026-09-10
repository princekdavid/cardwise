# CardWise Development Rules

## Status synchronization rule

1. Every meaningful implementation change must update the affected status/project-memory documents in the same implementation slice when practical.
2. Status must describe the actual branch/HEAD being developed, not a historical branch or stale commit.
3. Do not mark a feature or milestone `VERIFIED` based on implementation alone.
4. After GitHub Actions completes successfully for the exact implementation commit, update the affected status/project-memory documents with the verified commit/run evidence and promote the relevant state to `VERIFIED` only when the required evidence is complete.
5. If CI fails, keep the affected work `IN_PROGRESS` or `NEEDS_VERIFICATION`, record the failure, fix it, and rerun CI.
6. Never silently reconcile conflicting repository documents; record and resolve the discrepancy.
7. Every next-work selection must come from the latest verified repository state and the current work queue.

## Execution loop

`inspect latest state -> plan -> implement -> test -> update status -> CI -> inspect CI -> update status with evidence -> plan next item`

A green CI run is required before describing the corresponding change as complete.
