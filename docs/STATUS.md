# CardWise Project Status

Updated: 2026-09-06

## Milestones

| Milestone | Status | Notes |
|---|---|---|
| M0 Foundation | Complete | Merged to `main`; Android CI passed after Java/Kotlin JVM target alignment. |
| M1 Card Wallet | In progress | Card persistence, validation, ViewModel/StateFlow and list/add/detail/edit/delete UI are implemented on `feature/m1-card-wallet`. Network selection is included in the card form. CI must pass before completion. |
| M2 Rewards Intelligence | Planned | Deterministic benefit/reward modeling and eligibility rules. |
| M3 Recommendation Engine | Planned | Merchant-aware ranking and explainable recommendations. |
| M4 Scan | Planned | QR detection/parsing foundation. |
| M5 Scan & Pay | Planned | Payment-app discovery, recommendation and handoff without a second QR scan where platform capabilities allow. |
| M6 Beta Hardening | Planned | Security, performance, accessibility, release readiness and regression coverage. |

## Architecture Direction

```text
Compose UI
   -> ViewModel / StateFlow
      -> Domain validation + business rules
         -> Repository contract
            -> Local data source (Room)
```

- UI does not own persistence or business rules.
- Structured data is persisted locally for offline-first behavior.
- Card storage is limited to safe display metadata; never store PAN, CVV, PIN or full track data.
- Database access stays behind repository interfaces.
- Expensive work must remain off the main thread.
- Lists use stable keys and immutable UI state where practical.
- Animations must remain lightweight and avoid per-frame allocations.

## Performance Standards

- Lazy initialization for database/application dependencies.
- No blocking database work on the main thread.
- Stable Compose models and minimal unnecessary recomposition.
- `LazyColumn` with stable item keys for card lists.
- QR frame processing will be throttled and cancellation-aware.
- Recommendation calculations will be deterministic and optimized for in-memory execution.
- Release hardening will include R8/resource shrinking, startup profiling and baseline profiles where measurements justify them.

## Security & Privacy Standards

- Never persist full card numbers, CVV, PIN or track data.
- Validate card metadata before persistence.
- Keep secrets and credentials out of source control.
- Request only permissions required for a feature.
- QR scanning must avoid retaining raw payment payloads beyond the minimum processing lifetime unless explicitly required by product behavior.

## Test Matrix

### M0
- JVM unit tests
- Compose navigation smoke tests
- Debug APK assembly
- GitHub Actions CI

### M1
- Card domain model tests
- Card validation tests
- ViewModel tests with a fake repository
- Compose navigation/UI tests
- Room DAO and repository integration coverage is planned for the next CI expansion.
- Full wallet CRUD UI coverage is planned for the next CI expansion.

## CI Policy

A milestone is not complete while required CI is red or unverified. Build warnings may be tracked separately from failures. Current known workflow warnings include GitHub Actions Node/runtime deprecation notices and an Android native-library strip warning; neither is currently treated as a build failure.

## Current M1 Risks / Follow-ups

1. Expand CI to execute Android instrumentation tests on an emulator.
2. Add repository and in-memory Room integration tests to the automated CI path.
3. Expand automated UI coverage for add, list, detail, edit and delete flows.
4. Review Room schema export/migration testing before schema changes are introduced.
5. Replace temporary navigation placeholders with final product surfaces incrementally.
