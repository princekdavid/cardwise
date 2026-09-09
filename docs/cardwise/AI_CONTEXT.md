# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current implementation baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active design-integration branch: `feat/design-system-prototype-integration`
- Current branch HEAD: `4d21b26d6d561e2a99d17336cd1520837a59c287`
- Latest branch commit: `test(scan): cover merchant category reconciliation`
- PR: #11, `feat: integrate prototype design system and key screens`
- PR #11 is open and unmerged. Treat the current branch HEAD as the implementation baseline.
- Android CI: the latest verified run on the preceding HEAD was green; the current scan-reconciliation commits require a fresh CI run.

## Product promise

**Before I pay, CardWise tells me the smartest way to pay.**

CardWise is a privacy-first payment companion. It manages a user's cards and benefits, evaluates payment context, explains recommendations, and assists with QR-based UPI payment handoff without handling UPI PINs or banking credentials.

## Canonical project documents

Read these in order for a new session:

1. `docs/cardwise/AI_CONTEXT.md` — bootstrap context.
2. `docs/cardwise/KNOWLEDGE_GRAPH.yaml` — machine-readable project state, feature/engine/screen registry and work queue.
3. `docs/cardwise/DESIGN_SOURCE.md` — normalized visual language, tokens, components and small-change playbook.
4. `docs/cardwise/SCREEN_MATRIX.md` — screen/state/interaction reconciliation.
5. `docs/cardwise/UI_BACKEND_PLAN.md` — finalized screen-by-screen UI + backend execution plan.
6. `docs/cardwise/ENGINE_CATALOG.md` — business-engine contracts and extension rules.

Existing narrower documents remain useful: `docs/PRODUCT_SPEC.md`, `docs/ARCHITECTURE.md`, `docs/UX_SYSTEM.md`, `docs/ROADMAP.md`, `docs/BETA_HARDENING_CHECKLIST.md`, `docs/DEFINITION_OF_DONE.md`.

## Latest approved design artifacts

- `CardWise Interactive Experience Prototype.html` — latest known 2026-09-07.
- `CardWise Jetpack Compose Android Application.kt.txt` — latest known 2026-09-07.
- `CardWise_Complete_Product_Blueprint.txt` — supplied project reference for product intent and scope.

These are approved **reference artifacts**, not a production architecture. The single-file Compose artifact must not replace the repository's production architecture.

## Source-of-truth hierarchy

1. Current repository code and tests for actual implementation.
2. Latest approved design/reference artifacts for intended visual and interaction design.
3. `docs/cardwise/*` for consolidated intent, reconciliation, contracts and plan.
4. Historical product/architecture/UX/roadmap documents.

When sources conflict, record the discrepancy and resolve it explicitly; never silently guess.

## Protected constraints

- **Launcher logo is locked:** the existing APK launcher logo must remain exactly as-is. Do not redesign, recolor, replace, substitute or reinterpret it.
- Privacy-first architecture is mandatory.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent payment credentials.
- QR contents are untrusted input and should be processed locally where practical.
- External payment handoff requires explicit user action.
- Business logic must not be embedded in Compose UI.
- Recommendation results must be deterministic for identical inputs/rules and explainable.
- Dynamic card, offer and merchant data must remain provider/engine backed; prototype data is not production truth.
- `IMPLEMENTED` and `VERIFIED` are different states.

## What is already implemented / historically verified

- M0 foundation: Android/Compose scaffold, design tokens, navigation shell, CI and baseline tests.
- M1 wallet: card model/validation, Room persistence, repository boundary, list/add/edit/delete/detail and benefits.
- M2 rewards/benefits: reward rules, categories, caps/thresholds, deterministic calculations, benefit tracking/reminder policy, persistence and edge-case tests.
- M3 recommendation: payment context, eligibility, deterministic ranking, explainable result, UI/ViewModel/state flow and tests.
- M4 scan: CameraX/ML Kit QR scanner, local UPI parsing, invalid/unsupported handling, duplicate protection and lifecycle hardening.
- M5 scan & pay: scan→recommendation prefill, sanitized UPI URI, confirmation, safe launcher, lifecycle-safe return and instrumentation.
- M6 production hardening: privacy, scanner lifecycle, deterministic recommendation/explanation, handoff/no-handler handling, testability seams and CI verification.
- M7 historical work: accessibility, wallet persistence regression and recommendation performance hardening landed.

## Current wallet slice

The wallet/tactile-deck slice has been implemented and the latest preceding CI run is green. APK visual/persistence verification remains pending.

Implemented:
- shared physical-card presentation;
- tactile deck spotlight with stacked supporting cards;
- active-card preference for the spotlight;
- stable accessibility/test semantics for the deck and spotlight;
- card detail presentation in a Material 3 bottom sheet;
- persisted Active/Paused updates through `CardWalletViewModel`;
- edit/remove actions remain repository-backed;
- empty deck and catalogue entry point remain supported.

## Current scan reconciliation slice

The active slice is **Scan reconciliation** and is implemented UI + backend together.

Implemented in the current slice:
- UPI QR parsing remains local and raw QR payloads are not retained;
- optional merchant category code (`mc`) is preserved as normalized payment context;
- recommendation prefill consumes the scanned amount and maps only high-confidence MCCs to `groceries`, `dining`, `travel`, or `shopping`;
- unknown/ambiguous MCCs leave category empty so the user can choose rather than receiving a guessed recommendation category;
- a scanned QR without an amount now requires an explicit amount entry before the “Find best card” action proceeds;
- scanned payment context continues into recommendation and payment handoff without exposing payment credentials;
- non-UPI and malformed QR handling remains fail-closed.

Still required before Scan is considered verified:
- fresh CI for the scan reconciliation commits;
- instrumentation coverage for the missing-amount dialog and reconciled payment handoff;
- APK validation of real QR scanning, amount reconciliation, category prefill and non-UPI rejection;
- screenshot/video evidence if a UI failure is encountered (only when the environment provides capture capability).

## Finalized UI + backend direction

The next development stream is explicitly **UI + backend together**. For every screen we will implement:

- the intended user capability;
- the latest reference visual language;
- loading/content/empty/error/disabled states;
- the ViewModel/UI-state contract;
- domain/repository data ownership;
- engine/provider responsibilities where justified;
- unit/instrumentation coverage;
- APK/manual visual verification.

Execution order is documented in `UI_BACKEND_PLAN.md` and currently starts with shared UI primitives, then Cockpit, My Deck, Scan reconciliation, Reasoning, Recommendation, Handoff, Catalog, Offer Engine/Offers, Insights, Vault/Onboarding and final hardening.

## Current work queue

NOW:
1. Obtain genuine green Android CI for the current Scan reconciliation slice and inspect all logs.
2. Add/restore instrumentation coverage for missing-amount reconciliation and the scanned payment path.
3. Validate Scan behavior on APK, including real QR input, category context and safe handoff.
4. Reconcile the next Reasoning slice against the approved reference artifacts.
5. Keep memory synchronized with every meaningful implementation commit.

NEXT:
1. Recommendation/Reasoning visual and backend reconciliation.
2. Card Catalog provider/data contract.
3. Offer Engine contract/provider boundary and Offers UI.
4. Insights/Milestones history contract and UI.
5. Privacy Vault and onboarding/privacy oath.

LATER:
- Merchant Intelligence Engine.
- Additional reward/offer models and safe payment methods.
- Release hardening and visual regression depth.

## How to continue after a chat reset

1. Read this file.
2. Read `KNOWLEDGE_GRAPH.yaml`.
3. Read `DESIGN_SOURCE.md` and `UI_BACKEND_PLAN.md` for active design/implementation work.
4. Read `SCREEN_MATRIX.md` for the specific screen.
5. Verify current branch and HEAD in GitHub.
6. Check CI for that exact HEAD.
7. Pick the first `NOW` item unless the user changes priority.
8. Update affected memory documents in the same meaningful implementation slice.
