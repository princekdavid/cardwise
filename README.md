# CardWise

> Your intelligent payment companion — choose the smartest way to pay.

CardWise is a privacy-first Android application that helps users manage their cards, understand rewards and benefits, and eventually make smarter payment choices at the point of purchase.

## Product vision

**Before I pay, CardWise tells me the smartest way to pay.**

The long-term product combines:

- Card and benefit management
- Reward intelligence
- Merchant-aware recommendations
- Spend and reward insights
- Benefit reminders
- QR-based Scan & Pay assistance
- A scalable recommendation engine

## Development status

The repository is currently in **Foundation / M0: Product + Architecture**. Production feature development begins only after the architecture and design system are established.

## Documentation

- [Product specification](docs/PRODUCT_SPEC.md)
- [Architecture](docs/ARCHITECTURE.md)
- [UX and visual system](docs/UX_SYSTEM.md)
- [Development roadmap](docs/ROADMAP.md)

## Engineering principles

1. Privacy and security by design.
2. Business logic stays independent of UI.
3. Smooth interactions are designed, not added as decoration.
4. Offline-first behavior where practical.
5. Every feature has loading, empty, error and success states.
6. Performance and accessibility are release requirements.
7. `main` remains stable; meaningful changes are reviewed before merge.

## Planned stack

- Kotlin
- Jetpack Compose
- Coroutines + Flow
- Hilt
- Room + DataStore
- Retrofit/Kotlin serialization where networking is required
- Android barcode/QR capabilities
- JUnit + Compose UI testing
- GitHub Actions
