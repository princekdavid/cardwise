# CardWise Architecture

## 1. Direction

CardWise will use a modular Android architecture with a clear separation between presentation, domain logic and data. The first implementation should remain small while preserving boundaries needed for future growth.

## 2. Planned stack

- Kotlin
- Jetpack Compose
- Coroutines + Flow
- Hilt
- Room
- DataStore
- Retrofit + Kotlin serialization when remote APIs are introduced
- Android barcode/QR capabilities
- JUnit and Compose UI tests
- GitHub Actions

## 3. Logical layers

```text
UI / Compose
    |
ViewModel / UI State
    |
Domain / Use Cases
    |
Repositories
    |
Local data + Remote data
```

### Presentation

Owns screens, components, UI state and user interaction. It must not contain reward calculation or payment-ranking rules.

### Domain

Owns business rules such as reward estimation, eligibility, payment context and recommendation ranking. Domain code should be straightforward to unit test without Android UI dependencies.

### Data

Owns Room, DataStore, remote clients and repository implementations.

## 4. Planned module structure

```text
app/
core/
  common/
  design/
  database/
  network/
  security/
feature/
  home/
  cards/
  rewards/
  recommendation/
  scanner/
  settings/
domain/
  cards/
  rewards/
  payments/
  recommendations/
docs/
```

We may consolidate modules during the first implementation if multi-module Gradle overhead is not yet justified. Boundaries in code should exist before physical module splitting.

## 5. Core domain concepts

```text
Card
Issuer
RewardRule
Benefit
Merchant
PaymentContext
PaymentMethod
EligibilityResult
Recommendation
RewardEstimate
```

## 6. Recommendation pipeline

```text
PaymentContext
   -> normalize context
   -> determine eligible payment methods
   -> apply card/reward rules
   -> apply caps/thresholds
   -> calculate estimates
   -> rank candidates
   -> produce explainable Recommendation
```

The ranking engine must be deterministic for the same inputs and rules.

## 7. Security boundaries

- Never store UPI PINs.
- Never store banking passwords.
- Keep secrets out of source control.
- Use Android Keystore for keys/secrets that require protected storage.
- Minimize personal data.
- Treat QR contents as untrusted input.
- Require explicit user action before launching a payment flow.

## 8. Performance

- Prefer immutable UI state.
- Avoid unnecessary recomposition.
- Keep database/network work off the main thread.
- Lazy-load large lists.
- Use stable keys for dynamic lists.
- Measure scanner and recommendation latency.
- Avoid animation work that blocks rendering.

## 9. Offline behavior

The card wallet and user-configured reward rules should remain useful without a network connection. Remote data, if introduced, should degrade gracefully when unavailable.

## 10. Testing strategy

- Domain: unit tests for rules and calculations.
- Data: repository/database tests.
- UI: Compose interaction and state tests.
- Integration: scanner/payment handoff tests where platform APIs permit.
- Regression: every important recommendation rule gets a deterministic test case.
