# CardWise Project Status

Updated: 2026-09-08

## Current state

`main` contains the verified production foundation through Scan & Pay. The active design-integration branch reconciles the latest approved CardWise prototype/reference with the existing production Android architecture.

Current working branch: `feat/design-system-prototype-integration`.

Current HEAD: `b147d65ecc187febf15ea1d426ccd4b2702b9e4a`.

Current Android CI: run #378 passed for the current HEAD.

PR #11 remains open and unmerged. Branch HEAD is the implementation source of truth.

## Durable project memory

Start with:
- `docs/cardwise/AI_CONTEXT.md`
- `docs/cardwise/KNOWLEDGE_GRAPH.yaml`
- `docs/cardwise/DESIGN_SOURCE.md`
- `docs/cardwise/SCREEN_MATRIX.md`
- `docs/cardwise/UI_BACKEND_PLAN.md`
- `docs/cardwise/ENGINE_CATALOG.md`

## Latest design reference

The latest approved artifacts currently known are dated 2026-09-07:
- `CardWise Interactive Experience Prototype.html`
- `CardWise Jetpack Compose Android Application.kt.txt`

They define the intended visual/interaction direction. They do **not** replace the production Android architecture or authorize hard-coded mock data.

## Finalized implementation direction

We will implement **UI + backend together** screen-by-screen. Each slice must define:
- what the user capability should do;
- reference visual requirements;
- states and interactions;
- ViewModel/UI-state contract;
- domain/repository ownership;
- engine/provider contract where justified;
- tests and APK/manual verification.

Detailed order is in `docs/cardwise/UI_BACKEND_PLAN.md`.

## Planned execution order

1. Shared UI primitives/design tokens.
2. Cockpit UI + metrics/data-state boundary.
3. My Deck/Wallet visual reconciliation + Room/repository preservation.
4. Scan reconciliation.
5. Reasoning screen + deterministic evaluation trace.
6. Recommendation reconciliation.
7. Payment Handoff reconciliation + lifecycle states.
8. Card Catalog provider/data contract + UI.
9. Offer Engine + Offers UI.
10. Insights/Milestones history contract + UI.
11. Privacy Vault + Onboarding/privacy oath.
12. Full E2E flow and final accessibility/performance/security/release hardening.

## Product capabilities

### Verified/implemented core
- Card wallet and Room persistence.
- Reward-rule persistence and deterministic calculation.
- Eligibility and deterministic recommendation ranking.
- Explainable recommendations.
- Privacy-first QR scanner and UPI parsing.
- Scan → recommendation → explicit UPI handoff.
- Payment launcher seam and no-handler handling.
- Shared theme/design tokens and navigation foundation.

### Partial / integration
- Cockpit.
- Physical My Deck presentation.
- Card Catalog.
- Offers + Offer Engine.
- Reasoning presentation.
- Recommendation visual reconciliation.
- Handoff visual/state reconciliation.
- Onboarding/privacy oath.
- Insights/Milestones.
- Privacy Vault.

## Non-negotiable rules

- Existing APK launcher logo remains exactly as-is.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent credentials.
- QR data is untrusted and should remain ephemeral where practical.
- Payment handoff always requires explicit user action.
- Business logic belongs in domain/engines, not Compose UI.
- Dynamic card/offer/merchant data must be provider/engine backed.
- Never mark a feature `VERIFIED` without evidence.
- Prototype/reference data is visual fixture data, not production truth.

## CI policy

A milestone is not complete while required CI is red, blocked or unverified. Meaningful implementation changes should update the relevant project-memory documents in the same commit when practical. Trivial mechanical commits do not require semantic memory updates.
