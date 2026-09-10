# CardWise Offline-First Boundary

Updated: 2026-09-10

## Product rule

CardWise's decision flow is local-first. The app must not require internet access to scan/parse a payment context, evaluate enrolled cards and rules, show a recommendation, or prepare a sanitized UPI handoff.

## Works without internet

- Onboarding and privacy controls.
- Enrolled-card wallet/deck management using local persistence.
- QR parsing and payment-context normalization after a QR is captured.
- Recommendation and reward calculations from locally available cards/rules.
- Reasoning/decision trace generated from the local evaluation.
- Recommendation display, explanations and benefit math.
- Construction and validation of the sanitized `upi://pay` URI.
- Local payment-handoff outcome/history recording after the launcher boundary is invoked.
- Insights and milestones from locally persisted payment history.
- Bundled/curated catalog and offer data that is already present on-device.

## Network boundary

The Android app does not perform the payment itself. `AndroidUpiPaymentLauncher` creates a sanitized `upi://pay` intent and hands it to a UPI app on the device. The external UPI app owns network connectivity, authentication, PIN entry and payment completion.

CardWise must not block the handoff merely because CardWise has no network connection. A successful launcher result means the external UPI app was launched, not that the payment was completed.

## Offline behavior at handoff

1. CardWise validates the parsed payment fields locally.
2. CardWise builds the sanitized UPI URI locally; raw QR content is never forwarded.
3. CardWise launches the user's selected UPI app through Android's intent/chooser boundary.
4. If no UPI handler exists, CardWise reports `NoUpiApp` and keeps the user in CardWise.
5. If the payment context cannot be safely encoded, CardWise reports `UnsafePayment` and does not launch.
6. If the UPI app launches while the device is offline, CardWise does not retry, queue credentials, or claim payment success. The UPI app is responsible for its own network/error state.
7. Local history records the handoff outcome needed for insights; it must not record PINs, credentials or raw QR payloads.

## Cache/data direction

Provider-backed catalog and offer data should remain usable from the last known local snapshot. The current curated catalog/offer providers are bundled/local, so they do not introduce a runtime network dependency. Durable provider snapshots remain a future enhancement when live providers are introduced.

## Verification

`UpiPaymentHandoffOfflineFirstTest` codifies that URI construction is deterministic, uses only parsed payment fields, requires no network, and still works when the QR/payment context has no amount.

Future hardening should add device-level offline instrumentation covering the complete local decision flow and the external-application handoff boundary.
