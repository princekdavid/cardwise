# CardWise Screen & Interaction Matrix

This is the working reconciliation map between the approved CardWise reference journey and production Android implementation. `IMPLEMENTED` means code exists; `VERIFIED` requires evidence from tests/APK/manual validation.

## Conceptual screen set from the latest reference artifacts

| ID | Screen | Intended responsibility | Key states/interactions | Current status |
|---|---|---|---|---|
| S-001 | Onboarding | Explain promise/privacy and establish initial setup | Privacy oath, theme entry, setup/continue | DESIGNED / NEEDS VERIFICATION |
| S-002 | Cockpit | Premium home/dashboard and fast payment intelligence | Metrics, deck context, scan CTA, category-coded content | PARTIAL |
| S-003 | QR Scan | Capture supported UPI QR locally | Permission, scanning, duplicate detection, invalid/non-UPI handling | IMPLEMENTED; APK verification pending |
| S-004 | Reasoning | Show deterministic evaluation process | Loading/evaluation/provenance states | DESIGNED |
| S-005 | Recommendation | Present best card/payment method and why | Best match, benefit, caveats, continue | IMPLEMENTED; prototype reconciliation pending |
| S-006 | Handoff | Explicitly confirm and safely launch UPI app | Confirmation, cancel, launch, no-handler, return | IMPLEMENTED; APK/reference reconciliation pending |
| S-007 | Wallet / My Deck | Manage physical-card deck | Empty/populated, active/paused, detail/edit/delete | PARTIAL |
| S-008 | Card Catalog | Discover supported cards | Search, filters, category discovery, add | PARTIAL |
| S-009 | Offers | Discover active relevant offers | Offer list/detail, applicability, expiry | PARTIAL / engine contract pending |
| S-010 | Insights / Milestones | Explain historical value and progress | Savings, rewards, milestones | DESIGNED |
| S-011 | Privacy Vault | Privacy controls and local-data management | Transparency, reset data, settings | DESIGNED |

## Cross-screen state requirements

Every production screen should explicitly account for:

- Loading.
- Content/success.
- Empty.
- Error/recovery.
- Disabled/inapplicable actions where relevant.
- Accessibility semantics and content descriptions.
- Light/dark theme behavior.
- Large-font/font-scale behavior.
- Navigation/back behavior.
- Process/lifecycle recreation where applicable.

## Scan → Recommendation → Handoff state machine

```text
SCAN
 ├─ camera permission needed → permission explanation → user grants/denies
 ├─ unsupported/invalid QR → rejection/error → retry
 └─ valid UPI QR
       ↓
   ephemeral PaymentContext
       ↓
   REASONING / recommendation evaluation
       ├─ missing amount → amount input modal/state
       ├─ no eligible card → explain no-match + recovery
       └─ recommendation ready
             ↓
       RECOMMENDATION
             ↓
       HANDOFF confirmation
        ├─ Cancel → recommendation
        └─ Continue → external UPI app
                       ├─ no handler → error/recovery
                       └─ launched → lifecycle return
                                      ↓
                                post-handoff state
```

## Wallet states

- No cards: clear empty state + catalog entry point.
- Cards present: physical deck/card list.
- Active filter: inactive cards excluded from active view.
- Card detail: benefits, metadata and actions.
- Edit: update card configuration.
- Delete/remove: explicit destructive confirmation where appropriate.
- Persistence: state survives process/app lifecycle according to repository contract.

## Catalog states

- Search empty.
- Search results.
- Category/filter selected.
- No matching cards.
- Card detail.
- Add to deck.
- Provider/data unavailable.

## Offer states

- Active offers.
- Expired offer.
- Offer not applicable to selected card.
- Applicable to card/merchant/transaction.
- Missing/stale provider data.
- Offer benefit estimate with provenance.

## Recommendation explanation contract

Every recommendation surface should be able to answer:

1. What should I use?
2. What benefit is expected?
3. Why did this option win?
4. What limits/caveats apply?
5. Is the value a configured fact, calculation, remote fact or uncertain estimate?

## Prototype-specific interaction inventory to reconcile

The latest reference artifacts include or emphasize:

- Obsidian Dark / Pearl Bright theme switching.
- Privacy oath during onboarding.
- QR scan → reasoning → recommendation → payment handoff.
- Missing amount modal.
- Non-UPI QR rejection.
- Post-handoff confirmation.
- Card detail bottom sheet.
- Active/paused card behavior.
- Remove-card flow.
- Toast feedback.
- Bottom navigation around Cockpit / My Deck / QR Scan hero / Offers / Milestones.
- Privacy Vault / reset data.
- Airy Cockpit & category colors.
- On-device AR lock-on concept.
- Deterministic reasoning feed.
- Physical Card Spotlight & Math Provenance.
- App & Card Resolver Carousel.
- Tactile Physical Card Deck.
- Category-Coded Discovery.
- Active Card Promos.

These are **requirements to reconcile**, not permission to replace the existing production architecture or hard-code reference mock data.

## Validation rule

When a screen is changed:

1. Update this matrix if scope/state behavior changed.
2. Add or update Compose instrumentation tests for critical interactions.
3. Run unit + instrumentation/build CI.
4. Validate the running APK for visual/interaction requirements that automated tests cannot establish.
5. Mark `VERIFIED` only after evidence is recorded in project memory.
