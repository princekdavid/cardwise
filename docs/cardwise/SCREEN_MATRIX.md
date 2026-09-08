# CardWise Screen & Interaction Matrix

Updated: 2026-09-08

This is the production reconciliation map between the **latest approved CardWise reference artifacts** and the Android implementation. `IMPLEMENTED` means code exists; `VERIFIED` requires evidence from tests/APK/manual validation.

Reference artifacts:
- `CardWise Interactive Experience Prototype.html` — latest known 2026-09-07.
- `CardWise Jetpack Compose Android Application.kt.txt` — latest known 2026-09-07.

For normalized visual rules, see `docs/cardwise/DESIGN_SOURCE.md`. For execution order and backend ownership, see `docs/cardwise/UI_BACKEND_PLAN.md`.

## Screen matrix

| ID | Screen | Should do | Reference design | Current production | Backend/domain | Next verification |
|---|---|---|---|---|---|---|
| S-001 | Onboarding | Explain promise/privacy and establish setup | Zero-Credential Privacy, calm centered hero, privacy oath, titanium CTA | DESIGNED | onboarding/preferences | Implement + verify setup/reset |
| S-002 | Cockpit | Give premium home intelligence + fast scan | Airy layout, deck count, scan hero, savings context, category accents | PARTIAL | wallet + metrics; engines as needed | Visual + state reconciliation |
| S-003 | QR Scan | Capture supported UPI QR locally | Camera HUD, emerald reticle, scan line, privacy callout | IMPLEMENTED | Scanner + ENG-001 | APK/device visual validation |
| S-004 | Reasoning | Explain deterministic evaluation | Orbital core + staged reasoning feed | DESIGNED | ENG-003/004 trace | Implement + deterministic tests |
| S-005 | Recommendation | Show best route, benefit, why, caveats | Physical card spotlight, math/provenance, comparison | IMPLEMENTED | ENG-002/003/004 | Visual/state reconciliation |
| S-006 | Handoff | Explicitly confirm and launch payment | Resolver/carousel, explicit CTA, return confirmation | IMPLEMENTED | Payment launcher boundary | APK + lifecycle validation |
| S-007 | My Deck | Manage tactile enrolled cards | Physical card skins, stack, active/paused, detail sheet | PARTIAL | Room + wallet repository | Visual + CRUD/persistence validation |
| S-008 | Card Catalog | Discover/filter/add cards | Category-coded discovery, search, card skins | PARTIAL | catalog provider + wallet | Provider contract + UI |
| S-009 | Offers | Show active relevant promotions | Active Card Promos, expiry, provenance | PARTIAL | Offer provider + ENG-005 | Engine contract + UI |
| S-010 | Insights/Milestones | Explain historical value/progress | Savings + milestone cards/progress | DESIGNED | history + ENG-007 | Define data contract then implement |
| S-011 | Privacy Vault | Explain/control local data and reset | Manifest/provenance, on-device data, reset | DESIGNED | local data/privacy boundary | Reconcile claims + reset flow |

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

## Global component requirements

Each reusable component should define applicable:
- default;
- pressed;
- focused;
- disabled;
- loading;
- success;
- error;
- selected/active;
- accessibility semantics/content description.

Core families:
`CardWiseSurface`, `CardWiseElevatedSurface`, `TitaniumActionButton`, `StatusPill`, `CategoryChip`, `MerchantContextCard`, `RewardSummary`, `RecommendationCard`, `PhysicalCard`, `PhysicalCardDeck`, `ScanReticle`, `ReasoningStep`, `BottomNavDock`, `CardDetailSheet`, `ConfirmDialog`, `ToastBanner`, `LoadingState`, `EmptyState`, `ErrorState`.

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

## Detailed interaction inventory

### S-001 Onboarding

Reference:
- Local / Private / Deterministic badge.
- "Pay Smart. Pay Less." promise.
- Zero-Credential Architecture card.
- Explicit list of data CardWise does not access.
- Start Optimizing CTA.

Production rule:
- Never request unnecessary SMS/bank credential access.
- Consent/setup state belongs outside transient Compose state.

### S-002 Cockpit

Reference:
- CardWise Engine + Payment Cockpit header.
- Deck count.
- Scan Merchant QR hero.
- Quick payment context/calculation area.
- Optimized savings + evaluated-payment metric.
- Offers entry.

Production rule:
- Do not hard-code prototype metrics.
- Metrics must come from an explicit data boundary.

### S-003 QR Scan

Reference:
- Scan & Find header.
- Camera viewport.
- Emerald reticle/corner brackets.
- Scan line.
- On-device privacy message.

Required states:
- permission needed/granted/denied;
- scanning;
- valid UPI;
- duplicate;
- malformed/invalid;
- non-UPI;
- missing amount;
- lifecycle return.

### S-004 Reasoning

Reference:
- Orbital core.
- Synthesizing Optimal Route.
- Staged pipeline: parse → evaluate cards → apply promos/rules → ready.

Production rule:
- Display real evaluation stages/provenance only.
- Do not fabricate timing, test counts or AI claims.

### S-005 Recommendation

Reference:
- Decision Engine label.
- Merchant + MCC/VPA + amount.
- Optimal Choice badge.
- Net reward/yield.
- Physical winner card.
- Calculation/provenance.
- Why-not comparison.
- Pay CTA, copy UPI, adjust amount.

Required explanation contract:
1. What should I use?
2. What benefit is expected?
3. Why did it win?
4. What caveats/limits apply?
5. Is the value configured, calculated, remote or uncertain?

### S-006 Handoff

Reference:
- App/card resolver presentation.
- Candidate app state.
- Selected payment app/card.
- Explicit pay/continue action.

Required states:
- resolving;
- ready;
- cancel;
- no handler;
- launched;
- returned;
- paid/incomplete confirmation.

### S-007 My Deck

Reference:
- My Physical Deck header.
- Add button.
- Tactile card skins.
- Empty deck state.
- Card details.
- Active/paused.
- Remove.

Production rule:
- Room/repository remains authoritative for enrolled cards.
- Catalog products and user enrollment remain separate models.

### S-008 Catalog

Reference:
- Search by card/issuer/category.
- Category-coded discovery.
- Card identity/skin.
- Perks/network/fee metadata.
- Add-to-deck state.

Required states:
- initial;
- search results;
- no results;
- filter selected;
- already added;
- provider unavailable.

### S-009 Offers

Reference:
- Active Card Promos.
- Benefit highlight.
- Expiry/urgency.
- Terms.
- Source/provenance.

Required states:
- active;
- expired;
- applicable;
- not applicable;
- stale/missing provider data;
- provider error.

### S-010 Insights/Milestones

Reference direction:
- optimized/saved value;
- evaluation count;
- spend/reward summaries;
- milestone progress.

Production rule:
- No fake historical numbers.
- Define durable history before introducing ENG-007.

### S-011 Privacy Vault

Reference:
- Privacy Vault title/subtitle.
- Engine provenance manifest.
- On-device data management.
- Reset/wipe action.

Production rule:
- Show only runtime/build-generated provenance facts.
- Destructive reset should be explicit and recoverable.

## Modal/sheet/toast inventory

The reference includes:
- Missing amount modal.
- Non-UPI QR rejection dialog.
- Post-handoff confirmation drawer/modal.
- Card detail bottom sheet.
- Toast feedback.
- Global navigation/reasoning loader overlay.

These are shared interaction patterns and should be implemented as reusable Compose components where possible.

## Bottom navigation

Reference visual pattern:
- floating rounded dock;
- compact labels/icons;
- elevated center Scan action;
- emerald active state;
- contextual navigation for non-tab screens.

The exact final fifth destination must be reconciled with the latest approved product navigation before locking the production tab set.

## Scan → Recommendation → Handoff state machine

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
