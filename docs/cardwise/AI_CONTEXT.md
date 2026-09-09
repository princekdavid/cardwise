# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current implementation baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active design-integration branch: `feat/design-system-prototype-integration`
- Current branch HEAD at last implementation check: `c1069bb380ecea0c5300b541082a874eeaee8304`
- PR: #11, `feat: integrate prototype design system and key screens`
- PR #11 is open and unmerged. Treat the current branch HEAD as the implementation baseline.
- CI was green on the preceding baseline; the catalog slice must be CI-verified before being marked verified.

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

### Reasoning
Implemented:
- `RecommendationEngine.evaluate()` returns the normal recommendations plus a deterministic `RecommendationTrace`;
- trace stages cover payment-context normalization, eligible-card evaluation, benefit calculation and ranking;
- trace distinguishes matched/no-match outcomes;
- `RecommendationUiState.Ready` carries the trace;
- scanned payments now route Scan → Reasoning → Recommendation;
- Reasoning UI progressively reveals actual trace steps with short presentation-only motion;
- loading, empty and error states do not fabricate latency, provenance or AI claims.

Remaining design/verification follow-up is tracked for the visual reconciliation and critical-flow instrumentation.

### Recommendation
Implemented and CI-verified on the preceding baseline:
- deterministic winner recommendation;
- physical-card winner spotlight treatment;
- transparent benefit math/provenance including eligible spend, rate and caps;
- deterministic “Why not this card?” explanations for alternatives;
- Rescan and Adjust amount/category recovery actions;
- Continue to UPI handoff;
- instrumentation stabilization for animated reward content.

### Card Catalog
Implemented in the current development slice, pending CI verification and final APK/manual validation:
- `CardCatalogRepository` separates catalog consumers from provider/cache implementation;
- `CardCatalogueEngine` owns provider refresh and local resource-store access;
- `CardCatalogViewModel` owns query/filter/loading/empty/unavailable/cached-error state;
- catalog products use stable string `productId` identities;
- product metadata includes card type, network, annual fee, reward program, benefits and provenance/freshness metadata;
- a replaceable built-in local seed provider exists only as transitional data and explicitly uses UNKNOWN provenance;
- catalog UI no longer owns the catalogue list and routes add-to-deck through the existing wallet ViewModel/repository;
- no sensitive payment credentials are introduced by catalog enrollment.

Known remaining catalog work:
- replace the transitional local seed with verified issuer/network/partner providers;
- move catalog caching from process-local memory to durable local storage for process-death/offline behavior;
- complete provider-backed detail/terms reconciliation and APK visual validation.

## Finalized UI + backend direction

The execution order is:
`Baseline/CI → Shared UI primitives → Cockpit → My Deck → Scan reconciliation → Reasoning → Recommendation → Handoff → Catalog → Offer Engine + Offers → Insights → Vault/Onboarding → E2E + hardening`

For every screen implement the user capability, reference visual language, state model, ViewModel/UI-state contract, domain/repository ownership, engine/provider boundaries, tests and APK/manual verification. Keep production architecture authoritative; the single-file Compose reference is not a replacement architecture.

## Current work queue

NOW:
1. Verify the Card Catalog slice in CI and APK/manual validation.
2. Replace the transitional local catalog seed with a verified provider when a real source/connection is available.
3. Move catalog caching to durable local storage before claiming full offline/process-death support.
4. Keep memory synchronized with meaningful commits.

NEXT:
1. Offer Engine contract/provider boundary and Offers UI.
2. Insights/Milestones history contract and UI.
3. Privacy Vault and onboarding/privacy oath.

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
