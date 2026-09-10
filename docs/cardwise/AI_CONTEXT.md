# CardWise AI Project Context

> Bootstrap document for any future development session. Read this first, then follow the linked canonical documents.

## Current implementation baseline

- Repository: `princekdavid/cardwise`
- Platform: Android
- Language/UI: Kotlin + Jetpack Compose + Material Design
- Architecture direction: Presentation → ViewModel/UI State → Domain → Repository → Local/Remote data
- Default branch: `main`
- Active hardening branch: `feat/e2e-payment-hardening`
- Current HEAD must be verified directly in GitHub before each work session; documentation SHAs are not verification evidence.
- The latest checked hardening CI run built the app successfully but failed one Recommendation instrumentation assertion. Therefore the current hardening slice is **NEEDS_VERIFICATION**, not VERIFIED.
- PR #11 design integration remains a separate, open/unmerged integration line; it is not the current hardening baseline.

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
7. `docs/cardwise/DEVELOPMENT_RULES.md`

## Source-of-truth hierarchy

1. Current repository code and tests for actual implementation.
2. Latest approved design/reference artifacts for intended visual and interaction design.
3. `docs/cardwise/*` for consolidated intent, reconciliation, contracts and plan.
4. Historical product/architecture/UX/roadmap documents.

When sources conflict, record the discrepancy and resolve it explicitly; never silently guess.

## Non-assumption rule

A plan, roadmap, prior chat, status document, dependency, integration, test, environment or tool is not evidence that a capability exists or works. Verify the actual repository/tool/environment/design/CI state before relying on it. If a required capability is missing or ambiguous, stop and surface the blocker. Never fabricate screenshots, device results, CI results, test results, integrations, design details or provider data. `IMPLEMENTED` and `VERIFIED` remain separate states.

## Design source

The uploaded `CardWise Interactive Experience Prototype.html` is the active visual reference for UI reconciliation. Use its established visual language rather than inventing a parallel UI system: dark/light ambient surfaces, glass cards, emerald decision-engine accents, compact uppercase/monospace metadata, physical-card identity, staged reasoning/synthesis motion, payment-route bridge and explicit handoff presentation.

Prototype values, merchant names, provider names and reward figures are visual fixture data only. They must not be promoted to production truth without provider/engine provenance.

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
- `RecommendationEngine.evaluate()` returns normal recommendations plus deterministic `RecommendationTrace`;
- trace stages cover payment-context normalization, eligible-card evaluation, benefit calculation and ranking;
- scanned payments route Scan → Reasoning → Recommendation;
- Reasoning UI follows the uploaded prototype's centered synthesis composition, animated orbital core and decision-pipeline treatment;
- loading, empty and error states do not fabricate latency, provenance or AI claims.

Remaining follow-up: CI/device visual verification.

### Recommendation
Implemented:
- deterministic winner recommendation;
- prototype-aligned decision-engine hierarchy and payee summary;
- physical-card winner spotlight treatment;
- optimal-choice/net-reward emphasis;
- payment-route bridge between the selected card and UPI intent handoff;
- transparent benefit math/provenance;
- deterministic “Why not this card?” explanations;
- Rescan and Adjust amount/category recovery actions;
- Continue to UPI handoff.

A previous instrumentation assertion expected obsolete pre-reconciliation copy. It has now been updated to the current prototype-aligned `Optimal Choice`, net-reward and calculation-provenance contract. The fix requires a fresh exact-commit CI pass.

### Card Catalog
Implemented:
- `CardCatalogRepository` separates consumers from provider/cache implementation;
- `CardCatalogueEngine` owns provider refresh and local resource-store access;
- `CardCatalogViewModel` owns query/filter/loading/empty/unavailable/cached-error state;
- stable string `productId` identities and provenance/freshness metadata;
- transitional local seed provider explicitly uses UNKNOWN provenance;
- catalog UI routes add-to-deck through the existing wallet ViewModel/repository.

Known remaining catalog work:
- replace transitional local seed with verified issuer/network/partner providers;
- move catalog caching from process-local memory to durable local storage;
- complete provider-backed detail/terms reconciliation and APK visual validation.

### Offer Engine
Implemented:
- provider-backed offer contract and curated transitional provider;
- deterministic eligibility/benefit evaluation;
- expiry and minimum-spend handling;
- provenance/confidence metadata;
- repository/ViewModel-backed Offers UI.

Remaining: production verified providers and durable cache boundary.

### Insights / Milestones
Implemented:
- Room-backed payment history;
- deterministic category/reward aggregation;
- milestones and progress states;
- payment history recording after successful UPI launcher result.

Remaining: final product/visual/accessibility/security validation.

### Privacy Vault / Onboarding
Implemented:
- first-run privacy oath;
- persistent onboarding completion;
- Privacy Vault navigation;
- explicit destructive reset confirmation;
- reset clears local data and returns to onboarding;
- instrumentation coverage with isolated test fixtures.

Remaining: final product/visual/accessibility/security validation.

## Current hardening queue

NOW:
1. Verify the latest Recommendation instrumentation test fix with exact GitHub Actions CI; fix any remaining failures.
2. Only after the exact implementation is green, synchronize status/context with the successful commit/run evidence.
3. Verify whether an actual screenshot/device evidence path exists. If it does not, record the gap rather than inventing evidence and ask the user only if their action is required.
4. Close accessibility, semantic coverage and font-scale validation.
5. Close security/privacy audit of local data, logs and external payment intents.
6. Close persistence/offline/process-death validation.
7. Close performance and release validation.
8. Reconcile remaining UI surfaces: Cockpit → My Deck → Scan/Reasoning/Recommendation/Handoff.

NEXT:
- Replace transitional catalog/offer providers with verified external providers when sources/connections are actually available.
- Durable provider caching and stronger process-death/offline coverage.
- Merchant Intelligence Engine only when an independent merchant-resolution responsibility is justified.

## Execution discipline

For every session:
1. Read this file and canonical docs.
2. Verify current branch/HEAD in GitHub.
3. Check CI for that exact HEAD before selecting work.
4. Verify prerequisites/capabilities needed for the next item.
5. Implement only against evidence-backed requirements.
6. Test the exact change.
7. Update affected status/context documents.
8. Inspect the resulting exact CI run.
9. Do not call work VERIFIED until all required evidence exists.
10. Select the next item from the latest verified repository state and current queue.
