# CardWise Product Specification

## 1. Product promise

CardWise helps a user answer one question quickly:

> **What is the smartest way for me to pay right now?**

The answer should consider the user's eligible cards, payment methods, merchant context, reward rules, limits, offers and other configured benefits.

## 2. Target experience

CardWise should feel like a premium, intelligent fintech utility rather than a spreadsheet or conventional banking dashboard.

Core UX qualities:

- Fast
- Calm
- Trustworthy
- Clear
- Context-aware
- Delightful without being distracting

## 3. MVP scope

### M0 — Foundation
- Architecture
- Design system
- Navigation shell
- CI/test foundation
- Documentation

### M1 — Card Wallet
- Add/edit/remove cards
- Card details
- Issuer/card metadata
- User-specific configuration
- Benefits overview

### M2 — Rewards Intelligence
- Reward categories
- Reward rates
- Caps and thresholds
- Fees and renewal dates
- Benefit expiry/reminders
- Reward estimation

### M3 — Payment Recommendation
- Payment context model
- Eligibility rules
- Reward calculation
- Ranking engine
- Explainable recommendations

### M4 — Scan
- QR scanner
- QR parsing
- Payment URI/context extraction
- Safe handling of unsupported QR formats

### M5 — Scan & Pay
- Explore supported Android payment handoff flows
- Detect installed compatible payment apps where platform APIs permit
- Minimize repeated scanning
- Never handle or store UPI PINs or banking credentials

## 4. Explicit non-goals for early versions

- Storing banking passwords or UPI PINs
- Acting as a bank
- Performing transactions without explicit user action
- Scraping private banking sessions
- Claiming guaranteed reward outcomes when rules are uncertain

## 5. Recommendation principles

Recommendations must be explainable. A result should answer:

1. What should I use?
2. What benefit is expected?
3. Why is this the best option?
4. Are there relevant limits or caveats?

Example:

**Use HDFC Card X**

Estimated benefit: **₹42**

Reason: **Eligible dining reward rate is higher than your other configured cards.**

## 6. Trust model

Financial information is user-configured and may change. CardWise must distinguish between:

- Known facts
- User-provided data
- Calculated estimates
- External/remote data
- Uncertain or stale information

The UI should avoid presenting estimates as guaranteed rewards.
