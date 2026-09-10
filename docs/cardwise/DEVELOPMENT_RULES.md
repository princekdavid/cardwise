# CardWise Development Rules

## Non-negotiable no-assumption rule

1. A plan, roadmap, design, prior chat, status document, dependency declaration, integration, test, environment or tool is **not evidence that the capability exists or works**.
2. Before implementing or relying on a prerequisite, inspect the actual current repository, connected tool capability, design artifact, test environment or CI evidence as applicable.
3. If a required capability, file, connection, design detail, dependency, test environment, artifact or verification path is missing or ambiguous, **stop before making the dependent change and surface the blocker**. Ask the user only when the missing information/capability cannot be resolved safely from the available source of truth.
4. Never claim `implemented`, `working`, `passing`, `verified`, `complete`, `device-tested`, `screenshot-captured` or equivalent without direct evidence supporting that exact claim.
5. Never fabricate screenshots, device results, CI results, test results, integration state, design details, provider data or other evidence.
6. Plan ≠ reality. The latest repository state, tests, CI, connected tools and approved design/reference artifacts determine what actually exists.
7. When sources conflict, record the discrepancy and resolve it explicitly; never silently fill the gap with assumptions.

## Source-of-truth and synchronization rule

1. Current repository code/tests determine actual implementation state.
2. The latest approved design/reference artifacts determine intended visual and interaction design.
3. `docs/cardwise/*` documents consolidate intent, contracts, status and work queue; they must not override evidence from code/tests/CI.
4. Every meaningful implementation change must update the affected status/project-memory documents in the same implementation slice when practical.
5. Status must describe the actual branch/HEAD being developed, not a historical branch or stale commit.
6. Do not mark a feature or milestone `VERIFIED` based on implementation alone.
7. After GitHub Actions completes successfully for the exact implementation commit, update the affected status/project-memory documents with the verified commit/run evidence and promote the relevant state to `VERIFIED` only when the required evidence is complete.
8. If CI fails, keep the affected work `IN_PROGRESS` or `NEEDS_VERIFICATION`, record the exact failure, fix it, and rerun CI.
9. After meaningful changes, inspect the latest HEAD and exact CI state again before selecting the next work item.

## Verification rule

Use evidence appropriate to the claim:

- **Unit/domain behavior:** relevant automated tests.
- **Android UI/navigation:** instrumentation tests plus required UI/state assertions.
- **Build/release:** successful exact-commit CI build.
- **End-to-end payment journey:** successful instrumentation plus device/APK validation where required by the acceptance criteria.
- **Visual fidelity:** approved design/reference comparison and, when required, real rendered UI evidence.
- **Accessibility:** automated semantics checks plus required font-scale/touch/contrast/manual validation.
- **Offline behavior:** explicit offline tests; do not infer offline support from architecture alone.
- **Provider-backed data:** provider/source/provenance evidence; transitional fixtures are not production truth.

If the required evidence path is unavailable, mark the work `NEEDS_VERIFICATION` or `BLOCKED` rather than upgrading its status.

## Execution loop

`inspect latest state -> verify prerequisites -> plan -> implement -> test -> update status -> CI -> inspect exact CI -> update status with evidence -> plan next item`

A green CI run is required before describing the corresponding implementation change as complete. A green CI run alone is not sufficient when the acceptance criteria require device, visual, accessibility, security, offline or other additional evidence.
