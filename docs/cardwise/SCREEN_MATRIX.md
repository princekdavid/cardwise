# CardWise Screen & Interaction Matrix

Updated: 2026-09-09

This is the production reconciliation map between the latest approved CardWise reference artifacts and the Android implementation. `IMPLEMENTED` means code exists; `VERIFIED` requires evidence from tests/APK/manual validation.

## Screen matrix

| ID | Screen | Should do | Reference design | Current production | Backend/domain | Next verification |
|---|---|---|---|---|---|---|
| S-001 | Onboarding | Explain promise/privacy and establish setup | Zero-Credential Privacy, calm centered hero, privacy oath, titanium CTA | DESIGNED | onboarding/preferences | Implement + verify setup/reset |
| S-002 | Cockpit | Give premium home intelligence + fast scan | Airy layout, deck count, scan hero, savings context, category accents | IN_PROGRESS | wallet + Cockpit presentation state; metrics boundary still pending | Build + APK visual/state validation |
| S-003 | QR Scan | Capture supported UPI QR locally | Camera HUD, emerald reticle, scan line, privacy callout | IMPLEMENTED | Scanner + ENG-001 | APK/device visual validation |
| S-004 | Reasoning | Explain deterministic evaluation | Orbital core + staged reasoning feed | IN_PROGRESS | Recommendation Engine evaluation trace | Add orbital visual refinement + instrumentation/APK validation |
| S-005 | Recommendation | Show best route, benefit, why, caveats | Physical card spotlight, math/provenance, comparison | IMPLEMENTED | ENG-002/003/004 | Visual/state reconciliation |
| S-006 | Handoff | Explicitly confirm and launch payment | Resolver/carousel, explicit CTA, return confirmation | IMPLEMENTED | Payment launcher boundary | APK + lifecycle validation |
| S-007 | My Deck | Manage tactile enrolled cards | Physical card skins, stack, active/paused, detail sheet | IN_PROGRESS | Room + wallet repository | Visual + CRUD/persistence validation |
| S-008 | Card Catalog | Discover/filter/add cards | Category-coded discovery, search, card skins | PARTIAL | catalog provider + wallet | Provider contract + UI |
| S-009 | Offers | Show active relevant promotions | Active Card Promos, expiry, provenance | PARTIAL | Offer provider + ENG-005 | Engine contract + UI |
| S-010 | Insights/Milestones | Explain historical value/progress | Savings + milestone cards/progress | DESIGNED | history + ENG-007 | Define data contract then implement |
| S-011 | Privacy Vault | Explain/control local data and reset | Manifest/provenance, on-device data, reset | DESIGNED | local data/privacy boundary | Reconcile claims + reset flow |

## Reasoning reconciliation

### S-004 Reasoning

Reference intent:
- Orbital reasoning core.
- "Synthesizing Optimal Route" state.
- Staged pipeline: parse → evaluate cards → apply promos/rules → ready.

Implemented in this slice:
- `RecommendationEngine.evaluate()` now returns recommendations plus a deterministic `RecommendationTrace`.
- Trace records normalized payment context, eligible-card evaluation, benefit calculation and final ranking.
- Trace outcome distinguishes matched vs no-match without exposing credentials or pretending to be an AI black box.
- Scanned payments now route through S-004 before S-005 Recommendation.
- Reasoning UI progressively reveals the real trace steps with short purposeful motion; animation timing is presentation-only and is not reported as engine latency.
- Loading/error/empty states are represented without fabricated provenance.

Still pending:
- Refine the orbital visual treatment to match the approved reference more closely.
- Add/restore critical-flow instrumentation around the Scan → Reasoning → Recommendation transition.
- APK visual validation in Obsidian Dark and Pearl Bright.

## Global visual rules

- Target personality: premium + intelligent + calm.
- Obsidian Dark and Pearl Bright are both first-class themes.
- Prefer shared glass/elevated surfaces, restrained borders/shadows, generous spacing and strong financial-number hierarchy.
- Use semantic category colors as functional accents, not decoration.
- Use tactile physical-card treatment for card identities where data supports it.
- Use monospace for financial/provenance data where useful.
- Use a metallic/titanium primary CTA treatment for major actions.
- Use short, purposeful motion; avoid meaningless indefinite spinners.
- Keep all styling centralized in tokens/components; do not copy CSS literals into every screen.
- Launcher logo is immutable.

## Cross-screen state requirements

Every production screen must explicitly account for, where applicable:
- Loading.
- Content/success.
- Empty.
- Error/recovery.
- Disabled/inapplicable actions.
- Accessibility semantics and content descriptions.
- Light/dark theme behavior.
- Large-font/font-scale behavior.
- Navigation/back behavior.
- Process/lifecycle recreation.

## Scan → Reasoning → Recommendation → Handoff state machine

```text
SCAN
 ├─ permission needed → explanation → grant/deny
 ├─ invalid/non-UPI → rejection → retry
 └─ valid UPI
       ↓
   ephemeral PaymentContext
       ↓
   REASONING
       ├─ missing amount → amount input
       ├─ no eligible card → explain + recovery
       └─ ready
             ↓
       RECOMMENDATION
             ↓
       HANDOFF
        ├─ Cancel → Recommendation
        └─ Continue → external UPI app
                       ├─ no handler → recovery
                       └─ launched → lifecycle return
                                      ↓
                                post-handoff confirmation
```

## Validation rule

When a screen changes:
1. Update this matrix if scope/state behavior changes.
2. Update `DESIGN_SOURCE.md` only when a reusable design rule changes.
3. Add/update Compose instrumentation tests for critical interactions.
4. Run unit + instrumentation/build CI.
5. Validate the running APK for visual/interaction requirements automation cannot establish.
6. Record exact commit/CI/APK evidence.
7. Mark `VERIFIED` only after evidence exists.
