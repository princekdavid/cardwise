# CardWise Project Status

Updated: 2026-09-10

## Current state

`main` remains the stable production foundation through Scan & Pay. The current implementation candidate is the E2E payment-hardening branch.

Current working branch: `feat/e2e-payment-hardening`.

Current HEAD: `ce85fe1158893af941fdd52a4f062aa784f356e7`.

Latest implementation slice: `1a65bbf6a73af53f22043c839c5baed3fe0d3cab` (`test: expand critical payment handoff regression coverage`).

CI verification for the implementation slice was not available from the connected GitHub workflow-run endpoint at the last check; therefore the slice remains **not verified**. Documentation commits after it do not constitute implementation verification.

PR #11 remains open and unmerged. The design-integration branch is a separate integration line and must not be treated as the current hardening baseline.

## Status synchronization rule

After every meaningful implementation change, update the affected status/project-memory documents. After GitHub Actions is green for the exact implementation commit, update those documents again with the verified commit/run evidence. Never mark implementation `VERIFIED` before that evidence exists. See `docs/cardwise/DEVELOPMENT_RULES.md`.

## Current hardening work

The latest hardening slice expands critical payment regression coverage for:
- successful recommendation → explicit handoff → successful launcher result → payment-history recording;
- explicit handoff cancellation with no launcher invocation or history record;
- no available UPI app with no payment-history record.

These tests are committed but awaiting CI verification.

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
- Accessibility and font-scale validation.
- Visual/state/lifecycle validation.
- Security/privacy audit.
- Performance/release validation.
- Cockpit, My Deck and remaining visual reconciliation.
- Catalog provider/durable cache work.
- Offers, Insights, Privacy Vault and Onboarding product completion.

## Next execution order

1. Verify the critical payment E2E regression slice against its exact implementation commit.
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
