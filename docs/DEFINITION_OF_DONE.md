# CardWise Definition of Done

This document is the engineering checklist for deciding whether a CardWise change is complete. It is intentionally stricter than "the code compiles".

## 1. Universal rules

Every production-code change must:

- Preserve the privacy-first architecture.
- Keep business logic independent from Compose/UI code.
- Handle appropriate loading, empty, error, and success states.
- Avoid storing PAN, CVV, PIN, passwords, or banking credentials.
- Include or update tests for changed behavior and important edge cases.
- Keep user-facing explanations consistent with the actual calculation/state.
- Be deterministic where ranking, sorting, or recommendation decisions are involved.
- Build successfully before the change is considered ready.
- Have GitHub Actions complete successfully before the change is called complete.

If requirements are uncertain, choose the safer behavior and document the assumption rather than silently guessing.

## 2. Change-to-check matrix

| Change | Required verification |
|---|---|
| Domain/model/business logic | Unit tests + debug build |
| Recommendation engine/ranking | Unit tests, edge cases, deterministic tests + debug build |
| Reward/benefit calculation | Unit tests for thresholds, caps, rounding, invalid input + debug build |
| ViewModel/state | Unit tests + debug build |
| Compose UI | Unit tests where applicable + instrumentation + debug build |
| Navigation/app wiring | Instrumentation + debug build |
| Room/database/entities/DAOs | Unit tests + migration/database tests as applicable + instrumentation |
| DataStore/preferences | Unit tests + instrumentation as applicable |
| Networking/remote data | Unit tests with fakes/mocks + instrumentation for integration behavior as applicable |
| Gradle/dependency/plugin changes | Unit tests + debug build + instrumentation |
| GitHub Actions/workflow | Validate YAML/configuration + run the affected CI workflow |
| Security/privacy-sensitive code | Tests + manual review against privacy requirements + full CI |
| Documentation/design only | No Android test required unless behavior/contracts change |
| Mixed or uncertain change | Use the strictest applicable row; default to full CI |

## 3. Standard verification levels

### Level A — fast local verification

Use for isolated, low-risk domain/documentation changes:

```text
cd android
gradle testDebugUnitTest
gradle assembleDebug
```

### Level B — full Android verification

Use for UI, navigation, persistence, dependencies, or any change where instrumentation can be affected:

```text
cd android
gradle testDebugUnitTest
gradle assembleDebug
gradle connectedDebugAndroidTest --no-daemon
```

### Level C — CI verification

Every pushed feature/production change must be verified by GitHub Actions. A change is not complete until the relevant workflow run is **completed successfully**.

For the current Android CI, the expected sequence is:

```text
Build
 ├─ testDebugUnitTest
 └─ assembleDebug
       ↓
Instrumentation
 └─ connectedDebugAndroidTest
```

Instrumentation depends on the build job, so a build failure prevents meaningful instrumentation verification.

## 4. Test coverage checklist

For calculations, eligibility, recommendations, and benefits, consider at minimum:

- No cards.
- No eligible cards.
- Inactive/disabled cards.
- Missing or incomplete rules.
- Zero amount.
- Negative amount.
- Non-finite/invalid numeric input where the API can receive it.
- Minimum-spend thresholds.
- Maximum eligible spend.
- Reward caps.
- Multiple rules for the same category.
- Category mismatch and unknown category.
- Duplicate cards/rules.
- Equal rewards and deterministic tie-breaking.
- Floating-point/rounding behavior.
- Very large amounts.
- Benefit exhaustion.
- Expired benefits.
- Benefits expiring during the relevant period.
- Conflicting/overlapping benefits.
- Eligibility changing between calculation and display.
- Explanation matching the actual calculation.
- Insufficient confidence: do not claim a definitive best option when the data does not justify it.

Not every item must be implemented in every milestone. If a capability is not yet represented by the domain model, explicitly document it as future work rather than implying that it is already enforced.

## 5. UI acceptance checklist

For user-facing screens:

- Loading state is intentional and non-jarring.
- Empty state explains what the user can do next.
- Error state is actionable and does not expose sensitive details.
- Success/result state is clear about estimates versus guarantees.
- Important actions have appropriate accessibility semantics.
- Touch targets and text remain usable at larger font sizes.
- Animations support comprehension and do not block interaction.
- Navigation/back behavior is predictable.
- No sensitive card credentials appear in logs, screenshots, or UI.

## 6. Recommendation-specific acceptance

A recommendation is complete only when:

1. Eligibility is evaluated before ranking.
2. Inactive cards cannot win.
3. Reward rules are actually applicable to the transaction.
4. The selected rule produces the displayed reward.
5. Ranking is deterministic for equal outcomes.
6. The reason explains the actual winning rule/result.
7. Caps and limits are visible when material.
8. Unknown or stale information is not presented as guaranteed.
9. No eligible result is preferred over a misleading recommendation.

## 7. Database and persistence rule

If a feature introduces user-configured state that must survive app restarts, do not leave it only in an in-memory injection seam. Define the persistence model, DAO/repository contract, migration strategy, and tests before considering that persistence feature complete.

## 8. CI status language

Use these terms consistently:

- **Passing:** relevant checks completed successfully.
- **Running:** CI is currently executing; outcome is unknown.
- **Failed:** at least one required check failed.
- **Blocked:** a required check could not execute because a prerequisite failed or infrastructure prevented execution.
- **Complete:** implementation + required tests + required CI verification all pass.

Never describe a change as complete while required CI is still running or failing.

## 9. Milestone exit checklist

Before moving to the next milestone:

- [ ] Scope implemented.
- [ ] Architecture boundaries preserved.
- [ ] Edge cases reviewed.
- [ ] Unit tests added/updated.
- [ ] UI/instrumentation tests added where applicable.
- [ ] Documentation updated where behavior/contracts changed.
- [ ] Debug build passes.
- [ ] GitHub Actions passes.
- [ ] Known limitations explicitly recorded.
- [ ] No unresolved security/privacy concern.

## 10. Working rule for CardWise development

When in doubt, **inspect what changed first, select the strictest applicable verification level, run it, inspect failures, fix them, rerun, and only then report completion.**
