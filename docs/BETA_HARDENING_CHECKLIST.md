# CardWise M7 Beta Hardening Checklist

This checklist is the execution plan after M6 Scan & Pay production hardening.

## Accessibility

- [ ] Verify semantics/content descriptions on all actionable controls.
- [ ] Verify touch targets are usable at minimum supported size.
- [ ] Verify large-font layouts do not clip or hide critical actions.
- [ ] Verify scanner, recommendation and payment confirmation states are understandable without relying on color alone.
- [ ] Verify TalkBack navigation order on Home, Wallet, Recommendation and Scan flows.

## Regression coverage

- [ ] Add wallet add/edit/delete end-to-end coverage.
- [ ] Add empty/loading/error state coverage for wallet and recommendation flows.
- [ ] Add Scan permission denial/retry coverage.
- [ ] Add invalid/unsupported QR coverage through the UI.
- [ ] Add Scan → recommendation → cancel handoff coverage.
- [ ] Add Scan → recommendation → confirm → external handoff coverage.
- [ ] Add return-from-payment lifecycle coverage.

## Performance

- [ ] Measure cold startup on a release-like build.
- [ ] Inspect unnecessary Compose recompositions on primary screens.
- [ ] Confirm Room work remains off the main thread.
- [ ] Measure scanner frame-processing behavior and cancellation.
- [ ] Check large wallet lists for stable keys and predictable scrolling.
- [ ] Add baseline profile only if profiling demonstrates a meaningful startup/runtime benefit.

## Security and privacy

- [ ] Audit manifest permissions and exported components.
- [ ] Confirm backup policy does not expose sensitive app state.
- [ ] Search logs/source for accidental card credentials or raw payment payload persistence.
- [ ] Verify UPI payload sanitization at every external handoff boundary.
- [ ] Confirm camera permission is requested only when scanning is initiated.
- [ ] Review dependency versions and known security advisories before beta release.

## Persistence

- [ ] Verify Room schema export is version-controlled.
- [ ] Add migration tests for every schema version transition.
- [ ] Verify destructive migration is not used for user data in release configuration.
- [ ] Verify repository behavior across app restart and process recreation.

## Release readiness

- [ ] Define crash/error monitoring approach.
- [ ] Verify release signing/configuration outside source control.
- [ ] Enable appropriate release shrinking/optimization after validating reflection/serialization paths.
- [ ] Produce beta release notes and known limitations.
- [ ] Run full GitHub Actions CI on the release candidate commit.
- [ ] Perform final manual smoke test on a physical Android device.

## Exit criteria

M7 is complete only when all applicable items are verified, required automated tests pass, GitHub Actions is green, and known limitations are documented.
