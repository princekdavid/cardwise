# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current verified baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active design-integration branch: `feat/design-system-prototype-integration`
- Current branch tip at context creation: `1aa3343eca479be70be04e579b563e1bb6b3cb54`
- Latest branch commit: `refactor: apply shared cockpit spacing and motion`
- PR: #11, `feat: integrate prototype design system and key screens`
- PR #11 is open and not merged; its recorded head is `6a56dcda...`, while the branch has since advanced to `1aa3343e...`. Treat the branch tip, not the stale PR head metadata, as the current implementation baseline.

## Product promise

**Before I pay, CardWise tells me the smartest way to pay.**

CardWise is a privacy-first payment companion. It manages a user's cards and benefits, evaluates payment context, explains recommendations, and assists with QR-based UPI payment handoff without handling UPI PINs or banking credentials.

## Source-of-truth hierarchy

1. Current repository code and tests for what is actually implemented.
2. Latest approved CardWise design/reference artifacts supplied for the project:
   - `CardWise Interactive Experience Prototype.html` (latest known: 2026-09-07)
   - `CardWise Jetpack Compose Android Application.kt.txt` (latest known: 2026-09-07)
3. `docs/cardwise/KNOWLEDGE_GRAPH.yaml` for consolidated project intent, status and traceability.
4. `docs/cardwise/SCREEN_MATRIX.md` for screen/state coverage.
5. `docs/cardwise/ENGINE_CATALOG.md` for business-engine contracts and extension points.
6. Existing product/architecture/UX/roadmap documents for historical intent.

When sources conflict, do not silently guess. Record the discrepancy and resolve it explicitly.

## Protected constraints

- **Launcher logo is locked:** the existing APK launcher logo must remain exactly as-is. Do not redesign, recolor, replace, substitute or reinterpret it.
- Privacy-first architecture is mandatory.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent payment credentials.
- QR contents are untrusted input and should be processed locally where practical.
- External payment handoff requires explicit user action.
- Business logic must not be embedded in Compose UI.
- Recommendation results must be deterministic for identical inputs/rules and explainable.
- Dynamic card, offer and merchant data must remain provider/engine backed; do not hard-code prototype data into production behavior.
- `IMPLEMENTED` and `VERIFIED` are different states.

## What is already implemented / verified historically

- M0 foundation: Android/Compose scaffold, design tokens, navigation shell, CI and baseline tests.
- M1 wallet: card model/validation, Room persistence, repository boundary, list/add/edit/delete/detail and benefits.
- M2 rewards/benefits: reward rules, categories, caps/thresholds, deterministic calculations, benefit tracking/reminder policy, persistence and edge-case tests.
- M3 recommendation: payment context, eligibility validation, deterministic ranking, explainable result, UI/ViewModel/state flow and tests.
- M4 scan: CameraX/ML Kit QR scanner, local UPI parsing, invalid/unsupported handling, duplicate protection and lifecycle hardening.
- M5 scan & pay: scan→recommendation prefill, sanitized UPI URI, confirmation, safe launcher, lifecycle-safe return and instrumentation coverage.
- M6 production hardening: privacy, scanner lifecycle, deterministic recommendation/explanation, handoff/no-handler handling, testability seams and CI verification.
- M7 work already landed on historical/main: accessibility hardening (M7.1), wallet persistence regression hardening (M7.2), recommendation list performance hardening (M7.3) are present in commit history; do not assume the full current branch is CI-verified until CI is run against the current tip.

## Current design-integration work

The prototype/reference adds or emphasizes a richer product surface including Cockpit, Scan, Reasoning, Recommendation, Handoff, Wallet/My Deck, Catalog, Offers, Insights, Vault, onboarding/privacy oath, theme switching, card detail states, missing-amount and unsupported-QR handling, post-handoff confirmation, and bottom navigation.

The production code must be reconciled screen-by-screen with those artifacts. The reference single-file Compose implementation is **not** a replacement for the production repository architecture.

## Immediate next actions

1. Run CI against current branch tip `1aa3343e...` and record the result.
2. Finish screen-by-screen prototype/reference → production reconciliation.
3. Complete any remaining M7 beta hardening gaps that are still applicable after reconciliation.
4. Implement the agreed design-system/prototype screens incrementally without regressing existing wallet/recommendation/scan/pay architecture.
5. Keep engine/business contracts independent of UI.
6. Add/expand deterministic tests before marking each feature `VERIFIED`.

## How to continue after a chat reset

1. Read this file.
2. Read `KNOWLEDGE_GRAPH.yaml`.
3. Read `SCREEN_MATRIX.md` and `ENGINE_CATALOG.md` only for the area being changed.
4. Verify current branch and HEAD in GitHub.
5. Check CI for that exact HEAD.
6. Pick the first item under `NOW` in the knowledge graph unless the user explicitly changes priority.
7. Update the knowledge graph and relevant status docs in the same meaningful commit as the feature change.
