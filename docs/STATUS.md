# CardWise Project Status

Updated: 2026-09-07

## Current state

`main` contains the verified M0 foundation through the production-hardened Scan & Pay flow. The latest merged M6 commit is `7591de9ae4a25ee03e6a4448f13a54f15b43884f`.

The latest Android CI verification for that exact commit completed successfully (workflow run #151). The repository is now ready to move from feature construction into beta hardening rather than continuing to develop the already-merged historical feature branches.

## Milestones

| Milestone | Status | Notes |
|---|---|---|
| M0 Foundation | Complete | Android/Compose foundation, design system, navigation and CI are merged. |
| M1 Card Wallet | Complete | Card domain, validation, Room persistence, wallet CRUD/detail UI and tests are merged. |
| M2 Rewards & Benefit Intelligence | Complete | Reward rules, deterministic calculation, benefit tracking/reminder policy, persistence and tests are merged. |
| M3 Recommendation Engine | Complete | Payment context, eligibility, deterministic ranking, explanations, UI/ViewModel and tests are merged. |
| M4 Scan | Complete | CameraX/ML Kit scanner, local UPI parsing, invalid handling and lifecycle hardening are merged. |
| M5 Scan & Pay | Complete | Recommendation prefill, sanitized UPI handoff, confirmation and end-to-end validation are merged. |
| M6 Scan & Pay Production Hardening | Complete | Privacy, lifecycle, deterministic behavior, handoff safety, testability and CI verification are complete. |
| M7 Beta Hardening | Next | Accessibility, performance, security/privacy, regression depth and release readiness. |

## Verified engineering baseline

- UI remains separated from domain/business rules.
- Card credentials such as PAN/CVV/PIN are not persisted.
- QR payment payloads are handled locally and kept ephemeral.
- Recommendation ranking is deterministic and explainable.
- External payment handoff requires explicit user confirmation.
- Android CI runs unit/build verification followed by emulator instrumentation tests.
- The latest verified M6 CI run passed both build/unit and instrumentation stages.

## M7 execution order

1. **Accessibility audit:** semantics, content descriptions, touch targets, font scaling and navigation behavior.
2. **Regression coverage:** wallet CRUD, Scan & Pay states, cancellation/retry and lifecycle return paths.
3. **Performance:** startup, Compose recomposition, scanner processing and database access.
4. **Security/privacy:** permission review, exported components, logging, backup behavior and sensitive-data handling.
5. **Persistence hardening:** Room schema export/migration verification and upgrade-path tests.
6. **Release readiness:** crash/error strategy, release configuration, documentation and beta checklist.

## CI policy

A milestone is not complete while required CI is red, blocked, or unverified. Build warnings may be tracked separately from failures. Known non-blocking warnings must remain documented rather than silently ignored.

## Branch hygiene

Several historical `feature/m2-*`, `feature/m3-*`, and `feature/m7-*` branches remain in the repository. They should not be treated as active work when they are identical to `main` or contain obsolete pre-merge history. New work should branch from the current `main` baseline.
