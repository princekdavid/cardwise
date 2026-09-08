# CardWise Project Status

Updated: 2026-09-08

## Current state

`main` contains the verified foundation through the production-hardened Scan & Pay flow. The design-integration branch is the active working branch for reconciling the latest CardWise prototype/reference with the existing production Android architecture.

Current working branch: `feat/design-system-prototype-integration`.

The branch had reached `1aa3343eca479be70be04e579b563e1bb6b3cb54` (`refactor: apply shared cockpit spacing and motion`) before project-memory commits. The memory commits intentionally advance the branch; the canonical current HEAD must be refreshed after each meaningful commit.

PR #11 remains open and unmerged. Its recorded PR head is older than the current branch tip, so branch HEAD is the implementation source of truth.

## Durable project memory

Start with:

- `docs/cardwise/AI_CONTEXT.md` — fast bootstrap for a new development/chat session.
- `docs/cardwise/KNOWLEDGE_GRAPH.yaml` — canonical machine-readable project state, feature registry, engine registry, flows, constraints, decisions and work queue.
- `docs/cardwise/SCREEN_MATRIX.md` — screen/state/interaction reconciliation.
- `docs/cardwise/ENGINE_CATALOG.md` — engine responsibilities, contracts and future extension rules.

Existing documents remain authoritative for their narrower historical purpose:

- `docs/PRODUCT_SPEC.md`
- `docs/ARCHITECTURE.md`
- `docs/UX_SYSTEM.md`
- `docs/ROADMAP.md`
- `docs/BETA_HARDENING_CHECKLIST.md`
- `docs/DEFINITION_OF_DONE.md`

## Verified milestone baseline

| Milestone | Status | Notes |
|---|---|---|
| M0 Foundation | Complete | Android/Compose foundation, design system, navigation and CI. |
| M1 Card Wallet | Complete | Card domain, validation, Room persistence, wallet CRUD/detail UI and tests. |
| M2 Rewards & Benefit Intelligence | Complete | Reward rules, deterministic calculation, benefit tracking/reminder policy, persistence and tests. |
| M3 Recommendation Engine | Complete | Payment context, eligibility, deterministic ranking, explanations, UI/ViewModel and tests. |
| M4 Scan | Complete | CameraX/ML Kit scanner, local UPI parsing, invalid handling and lifecycle hardening. |
| M5 Scan & Pay | Complete | Recommendation prefill, sanitized UPI handoff, confirmation and lifecycle-safe return. |
| M6 Production Hardening | Complete historically | Privacy, lifecycle, deterministic behavior, handoff safety, testability and CI verification. |
| M7 Beta Hardening | In progress historically | Accessibility, wallet regression and recommendation performance work has landed; current branch still requires exact-HEAD verification and remaining applicable hardening. |
| Prototype/design integration | In progress | Screen-by-screen reconciliation with latest reference artifacts. |

## Product capabilities

### Implemented core

- Card wallet and Room persistence.
- Reward-rule persistence and deterministic reward calculation.
- Eligibility and deterministic recommendation ranking.
- Explainable recommendations.
- Privacy-first QR scanner and UPI parsing.
- Scan → recommendation prefill → explicit UPI handoff.
- Payment launcher test seam and no-handler handling.
- Shared theme/design tokens and navigation foundation.

### Partial / integration work

- Cockpit.
- Physical My Deck presentation.
- Card Catalog search/filter/discovery.
- Offers surface and provider-backed Offer Engine contract.
- Full prototype Reasoning presentation.
- Onboarding/privacy-oath experience.
- Insights/Milestones.
- Privacy Vault/settings.

### Upcoming

1. Exact-HEAD Android CI verification.
2. Prototype/reference → production screen reconciliation.
3. APK validation for visual and interaction fidelity.
4. Restore deferred full Scan → Recommendation → Handoff instrumentation coverage once the UI is ready.
5. Complete applicable beta hardening: accessibility, regression depth, performance, security/privacy, persistence and release readiness.
6. Introduce Offer Engine when its data contract is defined.
7. Introduce additional engines only when a cohesive responsibility needs independent rules, testing or evolution.

## Non-negotiable rules

- Existing APK launcher logo remains exactly as-is.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent credentials.
- QR data is untrusted and should remain ephemeral where practical.
- Payment handoff always requires explicit user action.
- Business logic belongs in domain/engines, not Compose UI.
- Dynamic card/offer/merchant data must be provider/engine backed.
- Never mark a feature `VERIFIED` without evidence.

## CI policy

A milestone is not complete while required CI is red, blocked or unverified. Meaningful implementation changes should update the relevant project-memory documents in the same commit when practical. Trivial mechanical commits do not require semantic documentation changes.
