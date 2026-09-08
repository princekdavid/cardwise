# CardWise Design Source & UI Rules

Updated: 2026-09-08

This document records the **approved visual language** for CardWise so small future design changes can be made without rediscovering the prototype.

## Design source hierarchy

1. **Current production Android code** — source of truth for what is implemented and behavior.
2. **Latest approved reference artifacts** — source of truth for intended visual/interaction design:
   - `CardWise Interactive Experience Prototype.html` — latest known 2026-09-07.
   - `CardWise Jetpack Compose Android Application.kt.txt` — latest known 2026-09-07.
3. This document — normalized design decisions extracted from those artifacts.
4. `docs/UX_SYSTEM.md` — durable UX/motion principles.
5. `docs/cardwise/SCREEN_MATRIX.md` — per-screen reconciliation and acceptance scope.

If a newer approved design artifact is supplied, update this document and the screen matrix **before** implementing it. Do not treat an older artifact as authoritative when a newer one exists.

## Non-negotiable visual constraint

**The existing APK launcher logo is locked and must remain exactly as-is.** The product UI may use the reference's shield/security iconography where appropriate, but this does not authorize changing the launcher asset.

## Visual personality

Target: **premium + intelligent + calm**.

The design should feel like a payment intelligence cockpit rather than a banking spreadsheet. Use generous whitespace, strong hierarchy, restrained glass surfaces, clear financial numbers, tactile card visuals and functional accent colors.

## Theme tokens

### Obsidian Dark

| Token | Value |
|---|---|
| Canvas | `#07090E` |
| Surface | `#10141E` |
| Elevated | `#171E2D` |
| Border | `#1E2638` |
| Primary accent / Quantum Emerald | `#10B981` |
| Neural Cyan | `#38BDF8` |
| Amber Alert | `#F59E0B` |
| Crimson Warning | `#F43F5E` |

Reference also uses a very dark body/canvas around `#05070B` / `#080B11` in the HTML phone shell. Android should prefer the shared production token set above rather than introducing another canvas token.

### Pearl Bright

| Token | Value |
|---|---|
| Canvas | `#F3F5F9` |
| Surface | `#FFFFFF` |
| Elevated | `#E8ECF2` |
| Border | `#CBD5E1` |

Reference light-mode body/phone-shell values (`#ECEFF4`, `#F8FAFC`) are shell details; use shared Android tokens for feature surfaces.

## Surface language

### Glass surface

Reference intent:
- Dark: translucent deep-blue surface, approximately 65% opacity, ~20px blur, subtle white border and restrained shadow.
- Light: translucent white surface, approximately 80% opacity, ~20px blur, soft slate border and shadow.

Android implementation should emulate the **visual effect**, not copy web CSS literally. Prefer Compose surfaces/alpha/blur where supported and keep the effect performant.

### Elevated glass

Use for primary decision cards, hero actions and important modal content.
- Dark reference: ~85% opacity, ~24px blur, stronger border/shadow.
- Light reference: ~95% opacity, ~24px blur, stronger soft shadow.

### Pills / chips

Use for filters, status labels and compact metadata. They should be visually quieter than primary actions.

### Cards

Use `RoundedCornerShape` consistently. The reference strongly favors approximately 18–20dp for major cards, ~12–16dp for controls, and a ~26dp floating navigation dock. Keep exact values centralized in tokens/components so a later radius change is one edit.

## Typography

Reference fonts:
- Primary: **Plus Jakarta Sans**
- Monospace / data: **JetBrains Mono**
- Card identity / embossed-number feel: **Space Grotesk**

Android should use bundled/available equivalents or configured font resources where licensing/distribution permits. Typography roles should be tokenized rather than hard-coded screen-by-screen.

Recommended semantic roles:
- Display/hero amount: bold/black, large.
- Screen title: bold.
- Section label: compact, uppercase, tracked.
- Body: regular/medium, comfortable line height.
- Financial/provenance data: monospace.
- Card identity: distinctive card font treatment where available.

## Spacing language

The reference is intentionally airy. Preserve a compact base rhythm and avoid dense lists.

Preferred working rhythm:
- 4dp micro spacing
- 6–8dp compact spacing
- 12dp control padding/section gap
- 14–16dp standard card padding
- 18–20dp major surface radius/padding
- 20dp screen horizontal inset for the primary mobile viewport

These are design targets, not permission to scatter literals through feature code. Centralize them.

## Buttons

### Titanium primary action

The reference primary CTA uses a light metallic gradient:
`#FFFFFF → #E2E8F0 → #CBD5E1`.

Characteristics:
- Full-width on key flows.
- ~50dp height for the reusable action primitive; some hero CTAs use slightly larger vertical padding.
- Strong dark text.
- Forward arrow or action icon when it improves scanability.

### Semantic actions

- Positive/active: Quantum Emerald.
- Informational: Neural Cyan.
- Attention/expiry: Amber.
- Destructive/error: Crimson.

Never rely on color alone; include text/icon/state.

## Physical card visual language

The reference uses realistic/tactile card identities rather than generic rectangles.

Known reference skins:
- Scapia: deep teal/green gradient, teal border, subtle teal shadow.
- Amazon Pay ICICI: charcoal/slate gradient, amber accent.
- Slice: violet/purple gradient, pale-violet border.
- HDFC Millennia: deep blue/cyan gradient.
- HDFC Swiggy: orange gradient.
- IndusInd Tiger: charcoal/brown/gold gradient.

Physical card treatment should include, where product data allows:
- issuer/card identity
- EMV/chip treatment
- contactless mark
- masked number only
- reward/perk highlight
- category badge
- active/paused state

Never fabricate sensitive card data. Only display masked/non-sensitive values intentionally stored by the product.

## Functional color semantics

Accent colors are category/meaning aids, not decoration:
- Emerald: optimized, active, verified, success.
- Cyan/sky: travel/information where applicable.
- Amber: shopping/attention/expiry where applicable.
- Orange: food/dining where applicable.
- Violet: Daily UPI/Slice-style category where applicable.
- Crimson: destructive/error/blocked.

The exact merchant/category mapping belongs to product data, not Compose styling.

## Motion language

Reference motion includes:
- short press/scale response
- subtle selection emphasis
- spring-like toggles
- restrained number transitions
- screen entrance continuity
- orbital/reticle effects for reasoning/scanning
- purposeful progress rather than indefinite generic spinners

Critical payment flow should feel continuous:
`Scan → Detect → Analyze → Recommend → Confirm → Pay → Return`.

Animations must be interruptible and respect reduced-motion/accessibility expectations.

## Global navigation

Reference mobile dock:
- Cockpit
- My Deck/Cards
- central elevated QR Scan hero
- Offers
- Privacy/Milestones depending on the final navigation decision

The current Android navigation should be reconciled against the latest approved reference rather than copied blindly. Navigation destinations that are not primary tabs should use contextual back actions.

## Reusable component inventory

The following should become or remain reusable Compose primitives/components:

- `CardWiseSurface` / glass container
- `CardWiseElevatedSurface`
- `TitaniumActionButton`
- `SecondaryButton`
- `StatusPill`
- `CategoryChip`
- `MerchantContextCard`
- `RewardSummary`
- `RecommendationCard`
- `PhysicalCard`
- `PhysicalCardDeck`
- `ScanReticle`
- `ReasoningStep`
- `BottomNavDock`
- `CardDetailSheet`
- `ConfirmDialog`
- `ToastBanner`
- `Loading/ReasoningState`
- `EmptyState`
- `ErrorState`

Components own styling and interaction states; screens compose them.

## Design change workflow

For a small future visual change:

1. Identify the affected token/component/screen in `SCREEN_MATRIX.md`.
2. If the change is global, change a token/component first.
3. If the change is screen-specific, update only that screen's composition.
4. Do not duplicate a one-off style if a reusable primitive already owns it.
5. Validate both Obsidian and Pearl themes.
6. Check accessibility, font scaling and touch targets.
7. Run relevant tests and record visual/manual verification.
8. Update this document only when the design rule itself changes; update the screen matrix for screen-specific changes.

## Prototype data rule

The reference contains example cards, offers, merchant data and reward values. Those values are useful for visual fixtures but are **not production truth**. Production UI must receive data from repositories/engines/providers.

## Current design reconciliation principle

When a reference screen is missing in Android:
- Reuse the same visual vocabulary above.
- Reuse existing production domain/repository contracts where they fit.
- Add the smallest new domain/engine contract necessary.
- Do not build a monolithic prototype screen or move business rules into Compose.
