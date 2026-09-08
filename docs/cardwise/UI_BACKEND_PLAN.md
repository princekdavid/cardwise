# CardWise UI + Backend Implementation Plan

Updated: 2026-09-08

## Goal

Bring the production Android app to the latest approved CardWise UI/reference experience while completing the backend/domain contracts required by those screens. The plan deliberately treats **UI and backend as one feature stream**: every screen gets a user-facing responsibility, state model, data contract and verification target.

The production architecture remains authoritative. The single-file Compose reference is a visual/interaction specification, not a replacement architecture.

## Delivery rules

1. Verify current branch/CI before feature work.
2. Reconcile the reference screen-by-screen before implementing large visual changes.
3. Reuse existing repositories, Room models and engines whenever they already satisfy the requirement.
4. Add an engine only when a cohesive responsibility needs independent rules, testing, provider/data boundaries or evolution.
5. Keep all business rules outside Compose.
6. Keep reference/mock card, offer and merchant data out of production behavior.
7. Every screen must cover loading, success/content, empty, error/recovery and disabled/inapplicable states where relevant.
8. Validate Obsidian Dark and Pearl Bright.
9. Do not mark `VERIFIED` without test/APK/manual evidence.
10. Keep the launcher logo exactly unchanged.

## Phase 0 — Baseline and reconciliation gate

**Status: READY / CI VERIFIED for current branch HEAD**

Current branch: `feat/design-system-prototype-integration`

Current HEAD: `b147d65ecc187febf15ea1d426ccd4b2702b9e4a`

Android CI run #378 completed successfully for this HEAD.

Before starting each UI slice:
- inspect current production screen/code;
- compare with latest approved HTML + Compose reference;
- record gaps in `SCREEN_MATRIX.md`;
- decide whether each gap is visual-only, interaction/state, domain, repository or engine work.

## Phase 1 — Foundation and shared visual language

**Objective:** make later screen work cheap and consistent.

### UI
- Centralize spacing, radii, typography roles, icon sizes, elevation and motion.
- Standardize glass/elevated surfaces, pills, buttons, cards, sheets, dialogs, toast, loading, empty and error components.
- Implement physical-card visual treatment as reusable UI, not per-screen markup.
- Ensure theme-aware tokens are used everywhere.

### Backend/domain
- Keep UI models separate from persisted/domain models where needed.
- Define stable presentation models for card, reward, offer, merchant, recommendation and insight content.
- Keep provenance/verification metadata available for financial calculations.

### Done when
- No feature screen needs to duplicate glass/card/button styling.
- Both themes render through shared tokens.
- Accessibility and font scaling are supported by the shared primitives.

## Phase 2 — Cockpit

**Feature: F-006 / Screen: S-002 / Status: PARTIAL**

### Should do
Provide the premium home surface that answers: "What can CardWise help me do right now?"

### UI
- Header: CardWise Engine + Payment Cockpit.
- Deck count/context.
- Hero Scan Merchant QR action.
- Amount/merchant context when quick calculation is useful.
- Optimized/savings metric with evaluated-transaction context.
- Category-coded content without visual overload.
- Offers entry point.
- Bottom navigation.
- Empty wallet variant.
- Loading/error states for repository/provider-backed content.

### Backend/domain
- Read enrolled-card count/status from wallet repository.
- Read aggregate savings/evaluation metrics from a defined history/insights boundary; do not keep prototype counters in Compose state.
- Expose a lightweight `CockpitUiState` assembled by ViewModel.
- Use Recommendation/Offer engines only through explicit contracts.

### Verification
- Screenshot/manual comparison to reference.
- Theme/font-scale/accessibility checks.
- Navigation and scan CTA instrumentation.

## Phase 3 — My Deck / Wallet

**Feature: F-007 / Screen: S-007 / Status: PARTIAL**

### Should do
Make enrolled cards feel like a tactile physical deck while retaining production wallet behavior.

### UI
- My Physical Deck header.
- Add-card entry.
- Physical card stack/list.
- Active/paused state.
- Card benefit highlight.
- Card detail bottom sheet.
- Edit and remove actions.
- Empty deck state → Catalog.
- Confirmation/recovery for destructive actions.

### Backend/domain
- Continue using Room + repository as the source of enrolled cards.
- Add/normalize card presentation metadata without leaking UI-specific state into persistence.
- Persist active/paused state.
- Keep catalog product data separate from user enrollment data.

### Verification
- CRUD regression tests.
- Process recreation/persistence tests.
- APK/manual visual comparison for all three reference card identities plus an empty deck.

## Phase 4 — Scan

**Feature: F-004 / Screen: S-003 / Status: IMPLEMENTED; APK reconciliation pending**

### Should do
Provide a calm, trustworthy on-device scanner that immediately communicates what is happening.

### UI
- Scan & Find header.
- Camera viewport.
- Emerald reticle/corner brackets.
- Scan-line motion where performant.
- Detection success state.
- Permission explanation/denial recovery.
- Invalid/non-UPI rejection.
- Privacy explanation.
- Missing-amount path when QR lacks amount.

### Backend/domain
- Preserve `QR / Payment Context Resolver`.
- Raw QR remains untrusted and ephemeral.
- Produce normalized `PaymentContext` for downstream reasoning.
- No credentials or PINs.

### Verification
- Camera/permission instrumentation.
- Valid UPI, malformed QR, non-UPI, duplicate scan and lifecycle tests.
- APK validation for camera UX and scanner states.

## Phase 5 — Reasoning

**Feature: F-003 / Screen: S-004 / Status: DESIGNED**

### Should do
Show the user how CardWise arrived at the decision without pretending the UI is an AI black box.

### UI
- Orbital reasoning core.
- "Synthesizing Optimal Route" state.
- Deterministic progress pipeline.
- Completed/current/pending step states.
- Provenance and timing only where real measurements exist.
- Graceful no-match/error state.

### Backend/domain
- Recommendation Engine exposes a structured evaluation trace or reasoning events.
- Trace should contain normalized context, eligibility decisions, rule applications, benefit calculations and ranking outcome.
- Do not expose fabricated latency or "AI" claims.

### Verification
- Deterministic trace tests.
- Same input/rules → same outcome/ordering.
- Missing data and no-eligible-card recovery.

## Phase 6 — Recommendation

**Feature: F-003 / Screen: S-005 / Status: IMPLEMENTED; reference reconciliation pending**

### Should do
Answer: what to use, expected benefit, why it won and what caveats apply.

### UI
- Decision Engine header.
- Merchant context card.
- Winner spotlight.
- Physical card identity.
- Expected benefit/net yield.
- Math/provenance section.
- "Why not this card?" comparison.
- Pay CTA.
- Copy UPI ID.
- Adjust amount / rescan.

### Backend/domain
- Continue Recommendation Engine + Reward Engine.
- Expand candidate data to accept applicable offers once Offer Engine exists.
- Preserve deterministic ranking and explainability.
- Mark estimates/remote facts with provenance.

### Verification
- Ranking unit tests.
- Explanation contract tests.
- Visual/manual comparison.
- Accessibility for financial amounts and explanations.

## Phase 7 — Payment Handoff

**Feature: F-005 / Screen: S-006 / Status: IMPLEMENTED; reference reconciliation pending**

### Should do
Make the transition to the external UPI app explicit, understandable and safe.

### UI
- Resolver/handoff presentation.
- Candidate app availability state.
- Selected payment app.
- Selected card/payment method.
- Explicit confirmation.
- Cancel → recommendation.
- Continue → external app.
- No-handler recovery.
- Return/post-handoff confirmation.
- Success/incomplete feedback.

### Backend/domain
- Keep launcher/payment handoff boundary outside recommendation engine.
- Sanitize generated UPI URI.
- Never store PIN/credentials.
- Track only non-sensitive post-handoff outcome needed for local insights.

### Verification
- Launcher seam tests.
- No-handler tests.
- URI safety tests.
- Lifecycle return tests.
- Restore deferred end-to-end instrumentation once UI reconciliation is complete.

## Phase 8 — Card Catalog

**Feature: F-008 / Screen: S-008 / Status: PARTIAL**

### Should do
Let users discover supported cards and add one to their deck.

### UI
- Card Catalog header.
- Search by name/issuer/category.
- Category chips.
- Card identity/skin.
- Perks/fee/network metadata.
- Added/already-in-deck state.
- Card detail.
- Empty/no-match state.
- Provider unavailable state.

### Backend/domain
- Define `CardCatalogRepository`/provider boundary.
- Separate catalog product from enrolled user card.
- Define stable catalog IDs.
- Define data freshness/verification metadata.
- Add-to-deck flows through wallet repository.

### Verification
- Search/filter tests.
- Provider unavailable recovery.
- Add-to-deck persistence regression.

## Phase 9 — Offers + Offer Engine

**Feature: F-009 / Screen: S-009 / Engine: ENG-005 / Status: PARTIAL/PLANNED**

### Should do
Surface relevant, active, explainable promotions rather than a generic coupon feed.

### UI
- Active Card Promos.
- Applicable-card context.
- Benefit highlight.
- Expiry/urgency.
- Terms.
- Source/provenance.
- Not-applicable explanation.
- Expired/stale/provider-error states.

### Backend/domain
Introduce Offer Engine only after the contract is implemented:
- inputs: merchant, payment context, wallet, offer catalog/provider, time;
- outputs: applicable offers, benefit estimate, eligibility reason, expiry/provenance;
- providers: issuer, network, merchant, partner;
- uncertainty/staleness must be explicit.

### Recommendation integration
Offer Engine may provide candidate benefits to Recommendation Engine, but Recommendation Engine remains responsible for final ranking.

### Verification
- Expiry/window tests.
- Card/merchant/network matching.
- Stale/unavailable provider behavior.
- Deterministic ranking when offers are included.

## Phase 10 — Insights / Milestones

**Feature: F-010 / Screen: S-010 / Engine: ENG-007 / Status: DESIGNED**

### Should do
Explain the value CardWise has created over time.

### UI
- Total optimized/saved.
- Payment count.
- Category trends.
- Reward summaries.
- Benefit utilization.
- Milestone cards with progress.
- Empty/history-not-enough state.

### Backend/domain
- First define a durable transaction/evaluation history contract.
- Then introduce Insights Engine.
- Calculations must be explainable and provenance-aware.
- Avoid fake historical numbers from the prototype.

## Phase 11 — Privacy Vault + Onboarding

**Features: F-011/F-012 / Screens: S-011/S-001 / Status: DESIGNED**

### Onboarding should do
- Explain product promise.
- Explain zero-credential architecture.
- Obtain explicit consent for local setup.
- Offer theme entry.
- Move to Cockpit only after setup.

### Privacy Vault should do
- Explain stored local data.
- Explain engine/provenance model without fake integrity claims.
- Provide reset/delete control.
- Confirm destructive reset.
- Return to onboarding after reset.

### Backend/domain
- Define onboarding completion/preferences persistence.
- Define privacy/data inventory contract.
- Ensure reset clears the intended Room/local data and transient state.
- Never claim a cryptographic manifest or test count unless generated from actual runtime/build evidence.

## Phase 12 — Final hardening

- Full Scan → Reasoning → Recommendation → Handoff → Return E2E instrumentation.
- Accessibility and font-scale matrix.
- Dark/light visual regression checks.
- Performance profiling: startup, recomposition, scanner, recommendation list, database.
- Security review: permissions, logs, exported components, backups and sensitive data.
- Error/empty/loading state completeness.
- Release build validation.

## Screen-to-backend ownership map

| Screen | UI responsibility | Primary domain/backend | Engine | Key data |
|---|---|---|---|---|
| Onboarding | Promise/privacy/setup | Onboarding/preferences | — | consent, theme |
| Cockpit | Home intelligence | Wallet + metrics | Recommendation/Offer as needed | deck, savings, context |
| Scan | QR capture | Scanner + payment context | ENG-001 | QR → PaymentContext |
| Reasoning | Explain evaluation | Recommendation trace | ENG-003/004 | trace/provenance |
| Recommendation | Show best route | Recommendation state | ENG-002/003/004/005 | candidates, benefit, caveats |
| Handoff | Confirm/launch/return | Payment handoff | — | sanitized URI, app state |
| My Deck | Manage cards | Wallet repository/Room | — | enrolled cards |
| Catalog | Discover/add cards | Catalog provider + wallet | — | catalog products |
| Offers | Discover applicable promos | Offer repository/provider | ENG-005 | offers/provenance |
| Insights | Explain historical value | History/metrics repository | ENG-007 | evaluations, rewards |
| Vault | Privacy/reset | Local data/privacy boundary | — | data inventory, reset |

## Definition of done for each screen

A screen is complete only when:

- visual structure matches the latest approved reference;
- all required interactions work;
- data is supplied by the correct repository/domain/engine boundary;
- loading/empty/error/disabled states are covered;
- both themes work;
- accessibility semantics/touch targets/font scaling are acceptable;
- relevant unit/instrumentation tests pass;
- APK/manual validation is complete for visual requirements;
- `SCREEN_MATRIX.md`, `KNOWLEDGE_GRAPH.yaml` and status docs are updated with evidence.

## Small design-change playbook

If the request is "make cards less rounded", change the shared radius token/component and validate all consumers.

If the request is "make the Cockpit scan CTA larger", change only the Cockpit composition or its named variant.

If the request is "change the emerald accent", update the semantic token first; do not hunt through individual screens.

If the request is "add a new card skin", add presentation metadata to the card catalog/product model and render through `PhysicalCard`; do not duplicate a new card component.

If the request is "change how reward is calculated", change the Reward/Recommendation engine contract and tests, not the screen.

If the request is "add live offers", implement the Offer provider boundary + Offer Engine first, then connect the UI.

## Planned execution order

`Baseline/CI → Shared UI primitives → Cockpit → My Deck → Scan reconciliation → Reasoning → Recommendation → Handoff → Catalog → Offer Engine + Offers → Insights → Vault/Onboarding → E2E + hardening`

This order intentionally front-loads shared visual primitives and the existing payment journey, then adds provider-backed capabilities. It avoids building UI around fake data and minimizes later refactoring.
