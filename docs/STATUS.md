# CardWise Project Status

Updated: 2026-09-10

## Current state

`main` remains the stable production foundation through Scan & Pay. The current implementation candidate is the E2E payment-hardening branch.

Current working branch: `feat/e2e-payment-hardening`.

Current HEAD: `0355dc73a857ea1d5440ac973bc15a8b70d64b27`.

Latest UI implementation slice: `0355dc73a857ea1d5440ac973bc15a8b70d64b27` (`feat(ui): align recommendation route with CardWise prototype`), preceded by reasoning-screen prototype alignment at `2a34a28daaae7c0497434d2fff844f6af70a3fd7`.

The latest UI slice is **awaiting CI verification**. Do not mark it VERIFIED until the exact implementation commit has a successful GitHub Actions run.

The uploaded CardWise Interactive Experience Prototype is now the visual reference for UI implementation. Its dark/light glass surfaces, emerald decision-engine treatment, physical-card identity, reasoning/synthesis motion, payment-route bridge and handoff presentation should guide all UI reconciliation. Prototype data remains visual fixture data, not production truth.

PR #11 remains open and unmerged. The design-integration branch is a separate integration line and must not be treated as the current hardening baseline.

## Status synchronization rule

After every meaningful implementation change, update the affected status/project-memory documents. After GitHub Actions is green for the exact implementation commit, update those documents again with the verified commit/run evidence. Never mark implementation `VERIFIED` before that evidence exists. See `docs/cardwise/DEVELOPMENT_RULES.md`.

## Current hardening work

Completed in this slice:
- Reasoning screen now follows the prototype's centered synthesis composition, animated orbital core, staged decision pipeline and local-processing disclosure.
- Recommendation screen now follows the prototype's decision-engine hierarchy with payee summary, Optimal Choice winner treatment, net-reward emphasis, physical-card identity, payment-route bridge and calculation provenance.
- Existing payment E2E regression coverage remains intact for successful handoff, cancellation and no-UPI-app behavior.

Still pending:
- CI verification of the exact latest UI implementation commit.
- Device/APK visual validation and screenshot evidence when device tooling is available.
- Accessibility, security/privacy, persistence and performance M7 closure.

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

### Partial / integration / hardening
- Critical Scan → Recommendation → Handoff → Return flow: implementation present; final beta verification pending.
- Prototype-aligned Reasoning and Recommendation visual reconciliation: implementation present; CI/device verification pending.
- Accessibility and font-scale validation.
- Security/privacy audit.
- Performance/release validation.
- Cockpit, My Deck and remaining visual reconciliation.
- Catalog provider/durable cache work.
- Offers, Insights, Privacy Vault and Onboarding product completion.

## Next execution order

1. Verify the latest prototype-aligned UI implementation against its exact CI run.
2. Run device/APK validation and capture evidence for Scan → Reasoning → Recommendation → Handoff → Return when device tooling is available.
3. Close accessibility, security/privacy, persistence and performance M7 checks.
4. Reconcile remaining UI surfaces: Cockpit → My Deck → Scan/Reasoning/Recommendation/Handoff.
5. Complete Card Catalog provider and durable cache boundary.
6. Complete Offers, then Insights, then Privacy Vault/Onboarding.
7. Run release-candidate CI and physical-device smoke test.

## Non-negotiable rules

- Existing APK launcher logo remains exactly as-is.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent credentials.
- QR data is untrusted and should remain ephemeral where practical.
- Payment handoff always requires explicit user action.
- Business logic belongs in domain/engines, not Compose UI.
- Dynamic card/offer/merchant data must be provider/engine backed.
- Never mark a feature `VERIFIED` without evidence.
- Prototype/reference data is visual fixture data, not production truth.
