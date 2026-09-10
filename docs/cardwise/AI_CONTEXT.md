# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current implementation baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active hardening branch: `feat/e2e-payment-hardening`
- Current hardening HEAD at start of this synchronization: `6161c1c9ad05edcf9402a34bb398485bf10effca`
- PR #11 design integration remains a separate, open/unmerged integration line; it is not the current hardening baseline.
- Latest hardening implementation commit: `1a65bbf6a73af53f22043c839c5baed3fe0d3cab` (`test: expand critical payment handoff regression coverage`); CI verification was still pending at the last recorded check.

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

Remaining follow-up: reconcile the visual reasoning core/current-step treatment and verify the complete payment journey in instrumentation.

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
Implemented:
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

### Offer Engine
Implemented:
- provider-backed offer contract and curated transitional provider;
- deterministic eligibility/benefit evaluation;
- expiry and minimum-spend handling;
- provenance/confidence metadata;
- repository/ViewModel-backed Offers UI.

### Insights / Milestones
Implemented:
- Room-backed payment history;
- deterministic category/reward aggregation;
- milestones and progress states;
- payment history recording after successful UPI launcher result.

### Privacy Vault / Onboarding
Implemented:
- first-run privacy oath;
- persistent onboarding completion;
- Privacy Vault navigation;
- explicit destructive reset confirmation;
- reset clears local data and returns to onboarding;
- instrumentation coverage with isolated test fixtures.

## Finalized UI + backend direction

The execution order is:
`Baseline/CI → Shared UI primitives → Cockpit → My Deck → Scan reconciliation → Reasoning → Recommendation → Handoff → Catalog → Offer Engine + Offers → Insights → Vault/Onboarding → E2E + hardening`

For every screen implement the user capability, reference visual language, state model, ViewModel/UI-state contract, domain/repository ownership, engine/provider boundaries, tests and APK/manual verification. Keep production architecture authoritative; the single-file Compose reference is not a replacement architecture.

## Current hardening queue

NOW:
1. Critical payment E2E instrumentation: scanned payment context → Reasoning → Recommendation → Handoff → successful UPI launcher → payment history/Insights update.
2. Offline/network resilience and explicit provider degradation behavior.
3. Accessibility, semantic coverage and font-scale validation.
4. Visual/state hardening across loading/error/empty, dark/light themes and lifecycle return.
5. Security/privacy audit of local data, logs and external payment intents.
6. Performance and release validation.

NEXT:
- Replace transitional catalog/offer providers with verified external providers when sources/connections are available.
- Durable provider caching and stronger process-death/offline coverage.
- Merchant Intelligence Engine when independent merchant-resolution responsibility is justified.

## How to continue after a chat reset

1. Read this file.
2. Read `KNOWLEDGE_GRAPH.yaml`.
3. Read `DESIGN_SOURCE.md` and `UI_BACKEND_PLAN.md` for active design/implementation work.
4. Read `SCREEN_MATRIX.md` for the specific screen.
5. Verify current branch and HEAD in GitHub.
6. Check CI for that exact HEAD.
7. Pick the first `NOW` item unless the user changes priority.
8. Update affected memory documents in the same meaningful implementation slice.
