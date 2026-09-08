# CardWise Engine Catalog

This document defines **what each engine is responsible for**, not merely where code currently lives. The goal is to let CardWise evolve by adding or splitting engines without coupling business logic to Compose UI.

## Engine principles

1. An engine owns a cohesive business responsibility.
2. Engines accept explicit inputs and return explicit outputs.
3. Engines are UI-independent.
4. Deterministic logic must be deterministic for identical inputs/rules.
5. Engines must expose enough provenance to explain important financial calculations.
6. An engine must not silently become a dumping ground for unrelated rules.
7. A new engine is justified when a responsibility is reusable, independently testable, independently evolving, or fed by a distinct data source.
8. Prototype/mock data must never become the production data contract merely because it appears in a design artifact.

## ENG-001 — QR / Payment Context Resolver

**Purpose:** Safely convert supported QR/UPI input into normalized payment context.

**Inputs**
- QR payload

**Outputs**
- normalized `PaymentContext`
- validation result/error

**Must**
- Treat QR data as untrusted.
- Validate URI shape and required fields.
- Keep payment credentials out of the model.
- Keep raw scanned data ephemeral unless there is an explicit product need.

**Must not**
- Authorize payments.
- Store UPI PINs or banking credentials.
- Decide which card is best.

**Future extensions**
- Additional payment URI formats.
- Merchant context enrichment.

---

## ENG-002 — Reward Engine

**Purpose:** Calculate expected reward/benefit for a card and transaction.

**Inputs**
- payment context
- card
- reward rules

**Outputs**
- reward estimate
- calculation/provenance information

**Responsibilities**
- Category matching.
- Reward rates.
- Caps and thresholds.
- Invalid/non-finite numeric protection.
- Deterministic calculation.

**Must not**
- Depend on Compose.
- Mutate wallet state.
- Claim guaranteed real-world reward when the rule is uncertain/stale.

**Future extensions**
- Cashback.
- Points.
- Miles.
- Category multipliers.
- Spend thresholds.
- Card-specific reward models.

---

## ENG-003 — Recommendation Engine

**Purpose:** Answer: **Which eligible payment option gives the user the best expected value for this transaction?**

**Inputs**
- payment context
- enrolled cards/payment methods
- reward rules
- applicable offers
- eligibility constraints
- optional user preferences

**Pipeline**

```text
normalize context
       ↓
determine eligibility
       ↓
apply card/reward rules
       ↓
apply caps/thresholds
       ↓
calculate expected value
       ↓
rank candidates deterministically
       ↓
produce recommendation + explanation + caveats
```

**Outputs**
- ranked candidates
- selected candidate
- expected benefit
- reasoning/provenance
- caveats/uncertainty

**Must**
- Be deterministic for identical inputs and rules.
- Explain why the winner was selected.
- Handle ties deterministically.
- Handle missing/invalid transaction information safely.

**Must not**
- Launch external payment applications.
- Modify wallet data.
- Depend on UI state.
- Treat promotional or estimated data as guaranteed.

**Future extensions**
- Offer weighting.
- Merchant-specific rules.
- User preferences.
- Travel-value models.
- Subscription detection.
- Additional payment methods.

---

## ENG-004 — Eligibility Engine

**Purpose:** Decide whether a card, reward rule, offer or payment method applies to a transaction.

**Inputs**
- card/payment method
- payment context
- reward rule
- offer
- constraints

**Outputs**
- eligible/ineligible
- machine-readable reason
- human-readable explanation where needed

**Possible rules**
- Merchant/category restrictions.
- Network restrictions.
- Spend limits.
- Card status.
- Offer validity window.
- Minimum/maximum transaction amount.

This responsibility currently overlaps with recommendation-domain validation. It should become a separately reusable engine when the rule set grows enough to justify independent ownership.

---

## ENG-005 — Offer Engine

**Status:** Planned.

**Purpose:** Discover, normalize and determine applicable promotions/offers.

**Inputs**
- merchant context
- payment context
- wallet
- offer catalog/provider
- time/date

**Outputs**
- applicable offers
- estimated benefit
- eligibility reason
- expiry/provenance

**Must**
- Be provider-backed.
- Track validity/expiry.
- Distinguish verified offer facts from estimates.
- Support card/merchant/network relationships.

**Must not**
- Hard-code live offers in Compose.
- Make unverified savings claims.
- Become coupled to a single UI screen.

**Future providers**
- Issuer offers.
- Network offers.
- Merchant offers.
- Partner offers.

---

## ENG-006 — Merchant Intelligence Engine

**Status:** Proposed.

**Purpose:** Resolve and enrich merchant identity/context when reliable information is available.

**Potential inputs**
- PaymentContext.
- MCC.
- VPA/domain hints.
- Merchant data sources.

**Potential outputs**
- normalized merchant identity
- category
- confidence
- contextual attributes

**Future extensions**
- MCC mapping.
- Merchant catalog.
- Local merchant resolution.
- Merchant-specific offer matching.

---

## ENG-007 — Insights Engine

**Status:** Proposed.

**Purpose:** Convert historical transaction/reward data into explainable savings, spending and benefit-utilization insights.

**Potential outputs**
- savings metrics
- category trends
- reward summaries
- benefit utilization
- milestones

This engine should not be introduced until the underlying history/data contract is clear.

---

## Future engine admission checklist

Before introducing a new engine, answer:

- What single responsibility does it own?
- What are its inputs and outputs?
- Which existing engine currently owns this responsibility?
- Why is separation beneficial now?
- Can it be unit-tested without Android UI?
- Does it need a different data/provider boundary?
- What invariants must always hold?
- How does it affect Recommendation Engine ranking?
- What user-facing explanation/provenance is required?
- What happens when its data is unavailable or stale?

If these questions cannot be answered, keep the logic in the appropriate existing domain boundary rather than prematurely creating another engine.
