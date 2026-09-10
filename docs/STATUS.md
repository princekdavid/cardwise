# CardWise Project Status

Updated: 2026-09-10

## Current state

`main` remains the stable production foundation through Scan & Pay. The current implementation candidate is the E2E payment-hardening branch.

Current working branch: `feat/e2e-payment-hardening`.

The branch has advanced beyond the previously recorded status pointer. The latest verified repository state must always be checked directly in GitHub before execution; documentation commit SHAs are not themselves verification evidence.

The uploaded CardWise Interactive Experience Prototype is the active visual reference for UI implementation. Its dark/light glass surfaces, emerald decision-engine treatment, physical-card identity, reasoning/synthesis motion, payment-route bridge and handoff presentation guide UI reconciliation. Prototype data remains visual fixture data, not production truth.

PR #11 remains open and unmerged. The design-integration branch is a separate integration line and must not be treated as the current hardening baseline.

## Verification state as of latest checked CI

Latest checked hardening CI run: `34508654331`.

- Build job: **SUCCESS** — unit tests, debug APK assembly and APK publication completed.
- Instrumentation job: **FAILURE**.
- The emulator successfully booted and **31 Android tests executed; 30 passed and 1 failed**.
- Failing test: `RecommendationScreenTest.enteringPurchase_showsTopRecommendation`.
- Failure: the test waited for the obsolete text `Recommended for this payment` for 5 seconds; the prototype-aligned Recommendation screen now renders `Optimal Choice`, net reward and calculation provenance instead.
- This is a **test/UI-contract mismatch**, not evidence that the recommendation engine itself failed.
- CI failure means the prototype-aligned slice remains `NEEDS_VERIFICATION`.

A follow-up test fix has been applied to assert the current prototype-aligned UI contract. The exact latest CI result for that fix must be checked before any work is promoted to VERIFIED.

## Current hardening work

Completed implementation in this slice:
- Reasoning screen follows the prototype's centered synthesis composition, animated orbital core, staged decision pipeline and local-processing disclosure.
- Recommendation screen follows the prototype's decision-engine hierarchy with payee summary, Optimal Choice winner treatment, net-reward emphasis, physical-card identity, payment-route bridge and calculation provenance.
- Critical payment E2E regression coverage exists for successful handoff, cancellation and no-UPI-app behavior.
- The failing Recommendation UI instrumentation assertion has been updated to the current prototype-aligned contract.
- The No-Assumption Rule is now a repository development rule.

Still pending:
- CI verification of the latest test-fix commit.
- Device/APK visual validation and screenshot evidence when an actual screenshot-capable device/tooling path is available.
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
- Prototype-aligned Reasoning and Recommendation visual reconciliation: implementation present; exact latest CI/device verification pending.
- Accessibility and font-scale validation.
- Security/privacy audit.
- Performance/release validation.
- Cockpit, My Deck and remaining visual reconciliation.
- Catalog provider/durable cache work.
- Offers, Insights, Privacy Vault and Onboarding product completion.

## Next execution order

1. Check CI for the latest Recommendation instrumentation test fix and fix any remaining failures.
2. Update status/context with exact successful CI evidence once the exact implementation is green.
3. Verify whether a real screenshot/device evidence path actually exists; if missing, mark that verification gap explicitly and ask the user only if their involvement is required.
4. Close accessibility, security/privacy, persistence and performance M7 checks.
5. Reconcile remaining UI surfaces: Cockpit → My Deck → Scan/Reasoning/Recommendation/Handoff.
6. Complete Card Catalog verified provider and durable cache boundary.
7. Complete Offers, then Insights, then Privacy Vault/Onboarding.
8. Run release-candidate CI and physical-device smoke test.

## Non-negotiable rules

- Never assume a planned feature, dependency, integration, design, test, environment or workflow exists or works because a plan says it should.
- Verify prerequisites before implementation and verify claims with direct evidence.
- If a required capability is missing or ambiguous, stop and surface the blocker rather than silently filling the gap.
- Never fabricate screenshots, device results, CI results, test results, integrations, design details or provider data.
- Existing APK launcher logo remains exactly as-is.
- Never store UPI PINs, banking passwords, PAN/CVV or equivalent credentials.
- QR data is untrusted and should remain ephemeral where practical.
- Payment handoff always requires explicit user action.
- Business logic belongs in domain/engines, not Compose UI.
- Dynamic card/offer/merchant data must be provider/engine backed.
- Never mark a feature `VERIFIED` without evidence.
- Prototype/reference data is visual fixture data, not production truth.
