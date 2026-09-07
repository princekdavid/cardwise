# M7.3 Performance & Responsiveness

## Goal

Improve UI rendering efficiency without changing CardWise behavior or the deterministic recommendation flow.

## Scope

- Keep recommendation list item identity stable.
- Provide explicit lazy-list content types so Compose can reuse compatible item composition slots efficiently.
- Avoid behavior changes, new dependencies, or speculative performance work without measurable evidence.

## Verification

M7.3 requires unit tests, Android instrumentation tests, and GitHub Actions verification before merge.
