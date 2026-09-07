# CardWise Roadmap

This roadmap reflects the implementation currently merged to `main`. Historical milestone names are retained where practical, but the delivery order is based on the verified repository state.

## M0 — Foundation

- [x] Product specification
- [x] Architecture direction
- [x] UX/motion principles
- [x] Android project scaffold
- [x] Design tokens
- [x] Navigation shell
- [x] CI
- [x] Baseline tests

## M1 — Card Wallet

- [x] Card model and validation
- [x] Room persistence and repository boundary
- [x] Card list
- [x] Add card flow
- [x] Edit/delete card
- [x] Card detail
- [x] Benefit overview
- [x] Loading/empty/error states

## M2 — Rewards & Benefit Intelligence

- [x] Reward rules
- [x] Categories
- [x] Caps/thresholds
- [x] Deterministic reward calculation
- [x] Benefit tracking
- [x] Renewal/expiry reminder policy
- [x] Persisted reward rules
- [x] Edge-case test coverage

## M3 — Recommendation Engine

- [x] Payment context
- [x] Eligibility validation
- [x] Deterministic ranking engine
- [x] Explainable recommendation result
- [x] Recommendation UI
- [x] ViewModel/state flow
- [x] Deterministic and edge-case tests

## M4 — Scan

- [x] Camera permission UX
- [x] CameraX/ML Kit QR scanner
- [x] Local UPI QR parsing
- [x] Unsupported/invalid QR handling
- [x] Duplicate detection protection
- [x] Scanner lifecycle/disposal handling

## M5 — Scan & Pay

- [x] Scanned payment → recommendation prefill
- [x] Sanitized UPI payment URI construction
- [x] Explicit confirmation before handoff
- [x] Safe external UPI app launch
- [x] Lifecycle-safe return handling
- [x] End-to-end instrumentation coverage

## M6 — Scan & Pay Production Hardening

- [x] Privacy-first QR handling
- [x] Scanner lifecycle hardening
- [x] Deterministic recommendation and explanation
- [x] Handoff validation and no-handler handling
- [x] Testability seams for payment launching
- [x] Full Android CI verification

## M7 — Beta Hardening (next)

- [ ] Accessibility audit and fixes
- [ ] UI/interaction polish and motion consistency
- [ ] Performance profiling and startup/recomposition review
- [ ] Security/privacy review
- [ ] Crash/error monitoring strategy
- [ ] Room migration/schema verification
- [ ] Broader wallet and Scan & Pay regression coverage
- [ ] Release checklist and beta-readiness documentation

## Delivery rule

`main` remains stable. Work lands through focused feature/chore branches, required tests, and successful GitHub Actions verification before merge.
