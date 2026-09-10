# CardWise Project Status

Updated: 2026-09-10

## Current state

`main` remains the stable production foundation through Scan & Pay. The current implementation candidate is the E2E payment-hardening branch.

Current working branch: `feat/e2e-payment-hardening`.

Current HEAD at this status update: `7c39b168f6e5a99fbe91650e38f74636e21b1149`.

The uploaded CardWise Interactive Experience Prototype is the active visual reference for UI implementation. Its dark/light glass surfaces, emerald decision-engine treatment, physical-card identity, reasoning/synthesis motion, payment-route bridge and handoff presentation guide UI reconciliation. Prototype data remains visual fixture data, not production truth.

PR #11 remains open and unmerged. The design-integration branch is a separate integration line and must not be treated as the current hardening baseline.

## Verification state

Latest checked hardening CI run: `34508654331`, for commit `16e513d97b79cca83dd79af4f9c2e52d53b44e32`.

- Build job: **SUCCESS** — unit tests, debug APK assembly and APK publication completed.
- Instrumentation job: **FAILURE**.
- The emulator booted and **31 Android tests executed; 30 passed and 1 failed**.
- Failing test: `RecommendationScreenTest.enteringPurchase_showsTopRecommendation`.
- Failure: the test waited for obsolete pre-reconciliation copy `Recommended for this payment`; the current prototype-aligned Recommendation screen renders `Optimal Choice`, net reward and calculation provenance.
- This is a **test/UI-contract mismatch**. It is not evidence that the deterministic recommendation engine failed.
- Follow-up commits `dec56dbf1c88e0a171cf77b1cde4d440d77a5402` and `f1a40053be91a8b198dd372d909af6a8b5304825` update the instrumentation assertion to the current UI contract.
- Because the follow-up commit has not yet received a successful exact-commit CI result, the affected UI slice remains **NEEDS_VERIFICATION**.

## Current hardening work

Completed implementation:
- Reasoning screen follows the prototype's centered synthesis composition, animated orbital core, staged decision pipeline and local-processing disclosure.
- Recommendation screen follows the prototype's decision-engine hierarchy with payee summary, Optimal Choice winner treatment, net-reward emphasis, physical-card identity, payment-route bridge and calculation provenance.
- Critical payment E2E regression coverage exists for successful handoff, cancellation and no-UPI-app behavior.
- Recommendation instrumentation assertions were updated to the current prototype-aligned contract after CI exposed stale test copy.
- The No-Assumption Rule is now formalized in `docs/cardwise/DEVELOPMENT_RULES.md`, `AI_CONTEXT.md` and `KNOWLEDGE_GRAPH.yaml`.

Still pending:
- Fresh exact-commit CI verification after the test fix.
- Device/APK visual validation and screenshot evidence when an actual screenshot-capable device/tooling path is available.
- Accessibility, security/privacy, persistence/offline and performance M7 closure.

## Product capabilities

### Core implementation present
- Card wallet and Room persistence.
- Reward-rule persistence and deterministic calculation.
- Eligibility and deterministic recommendation ranking.
- Explainable recommendations.
- Privacy-first QR scanner and UPI parsing.
- Scan → recommendation → explicit UPI handoff.
- Payment launcher seam and no-handler handling.
- Shared theme/design tokens and navigation foundation.

### Partial / integration / hardening
- Critical Scan → Recommendation → Handoff → Return flow: implementation present; final verification pending.
- Prototype-aligned Reasoning and Recommendation visual reconciliation: implementation present; fresh CI/device verification pending.
- Accessibility and font-scale validation.
- Security/privacy audit.
- Persistence/offline/process-death validation.
- Performance/release validation.
- Cockpit and My Deck remaining visual reconciliation.
- Catalog verified provider/durable cache work.
- Offers, Insights, Privacy Vault and Onboarding final product/verification work.

## Next execution order

1. Check CI for the latest Recommendation test-fix HEAD and fix any remaining failures.
2. Once exact CI is green, synchronize status/context/knowledge graph with that exact commit and run evidence.
3. Verify whether an actual screenshot/device evidence path exists. If missing, record the gap and ask the user only when their action is required.
4. Close accessibility, security/privacy, persistence/offline and performance M7 checks.
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
