# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current implementation baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active design-integration branch: `feat/design-system-prototype-integration`
- Current branch HEAD at last implementation check: `487bb6f9ae56c1c23b6551c833150403a459563a`
- PR: #11, `feat: integrate prototype design system and key screens`
- PR #11 is open and unmerged. Treat the current branch HEAD as the implementation baseline.
- CI workflow now includes a manual `workflow_dispatch` trigger; fresh verification is still pending for the current head.

## Product promise

**Before I pay, CardWise tells me the smartest way to pay.**

CardWise is a privacy-first payment companion. It manages a user's cards and benefits, evaluates payment context, explains recommendations, and assists with QR-based UPI payment handoff without handling UPI PINs or banking credentials.

## Canonical project documents

Read these in order for a new session:
1. `docs/cardwise/AI_CONTEXT.md`
2. `docs/cardwise/KNOWLEDGE_GRAPH.yaml`
3. `docs/cardwise/DESIGN_SOURCE.md`
4. `docs/cardwise/SCREEN_MATRIX.md`
5. `docs/cardwise/UI_BACKEND_PLAN.md`
6. `docs/cardwise/ENGINE_CATALOG.md`

## Source-of-truth hierarchy

1. Current repository code and tests for actual implementation.
2. Latest approved design/reference artifacts for intended visual and interaction design.
3. `docs/cardwise/*` for consolidated intent, reconciliation, contracts and plan.
4. Historical product/architecture/UX/roadmap documents.

When sources conflict, record the discrepancy and resolve it explicitly; never silently guess.

## Protected constraints

- **Launcher logo is locked:** the existing APK launcher logo must remain exactly as-is. Do not redesign, recolor, replace, substitute or reinterpret it.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent payment credentials.
- QR contents are untrusted and should be processed locally where practical.
- External payment handoff requires explicit user action.
- Business logic must not be embedded in Compose UI.
- Recommendation results must be deterministic for identical inputs/rules and explainable.
- Dynamic card, offer and merchant data must remain provider/engine backed; prototype data is not production truth.
- `IMPLEMENTED` and `VERIFIED` are different states.

## Current slices

### Scan reconciliation
Implemented:
- local UPI parsing with optional MCC context;
- high-confidence MCC category prefill only;
- explicit amount entry for QR codes without amount;
- safe transition into recommendation and handoff;
- fail-closed malformed/non-UPI behavior.

### Reasoning — current slice
Implemented:
- `RecommendationEngine.evaluate()` returns the normal recommendations plus a deterministic `RecommendationTrace`;
- trace stages cover payment-context normalization, eligible-card evaluation, benefit calculation and ranking;
- trace distinguishes matched/no-match outcomes;
- `RecommendationUiState.Ready` carries the trace;
- scanned payments now route Scan → Reasoning → Recommendation;
- Reasoning UI progressively reveals actual trace steps with short presentation-only motion;
- loading, empty and error states do not fabricate latency, provenance or AI claims.

Still required:
- refine the orbital visual treatment against the approved reference;
- add/restore critical-flow instrumentation around Scan → Reasoning → Recommendation;
- fresh CI verification;
- APK visual validation in both themes.

## Finalized UI + backend direction

The execution order is:
`Baseline/CI → Shared UI primitives → Cockpit → My Deck → Scan reconciliation → Reasoning → Recommendation → Handoff → Catalog → Offer Engine + Offers → Insights → Vault/Onboarding → E2E + hardening`

For every screen implement the user capability, reference visual language, state model, ViewModel/UI-state contract, domain/repository ownership, engine/provider boundaries, tests and APK/manual verification. Keep production architecture authoritative; the single-file Compose reference is not a replacement architecture.

## Current work queue

NOW:
1. Verify Android CI for the current branch head using the automatic/manual workflow path.
2. Inspect all build and instrumentation logs and fix only verified failures.
3. Refine the Reasoning orbital visual treatment and add critical-flow instrumentation after CI stability is established.
4. Validate Scan → Reasoning → Recommendation on APK and both themes.
5. Keep memory synchronized with meaningful commits.

NEXT:
1. Recommendation visual/state reconciliation.
2. Payment Handoff visual/state reconciliation.
3. Card Catalog provider/data contract.
4. Offer Engine contract/provider boundary and Offers UI.
5. Insights/Milestones history contract and UI.
6. Privacy Vault and onboarding/privacy oath.

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
