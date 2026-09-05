# CardWise UX & Motion System

## 1. Experience principles

CardWise should communicate intelligence through clarity, not visual noise.

- One primary action per screen.
- Important information appears before secondary detail.
- Progressive disclosure keeps complex reward rules understandable.
- Every async operation has an intentional loading state.
- Every failure explains what happened and what the user can do next.
- Accessibility is part of the design, not a later pass.

## 2. Visual direction

Target personality: **premium + intelligent + calm**.

The visual system should use strong hierarchy, generous spacing, restrained surfaces, clear financial numbers and distinctive card/benefit visuals.

Avoid making the home screen resemble a spreadsheet.

## 3. Component families

```text
CardTile
PaymentRecommendation
RewardSummary
BenefitCard
MerchantContext
PrimaryButton
SecondaryButton
Chip
Badge
BottomSheet
Dialog
Skeleton
EmptyState
ErrorState
ScanOverlay
```

Every reusable component should define default, pressed, focused, disabled, loading, success and error behavior where applicable.

## 4. Motion language

Animation is used to explain state changes and preserve continuity.

### Micro motion

- Press: short scale/elevation response.
- Selection: subtle emphasis transition.
- Toggle: spring-based state change.
- Number update: restrained count transition.

### Navigation

Prefer shared visual continuity between related screens. Avoid excessive page transitions that slow frequent tasks.

### Recommendation reveal

The Scan & Pay flow should feel continuous:

```text
Scan -> Detect -> Analyze -> Recommend -> Pay
```

Each stage should communicate progress rather than feel like an unrelated screen.

### Loading

Use skeletons or purposeful progress indicators for content. Do not use indefinite spinners when meaningful progress can be communicated.

## 5. Scan screen direction

The scanner should provide a clear camera frame, guidance, successful detection feedback and a graceful unsupported-QR state.

After detection, transition into recommendation rather than abruptly navigating away.

## 6. Accessibility

- Respect system font scaling.
- Maintain sufficient touch targets.
- Never rely only on color to communicate state.
- Provide content descriptions for meaningful icons.
- Ensure animation can be reduced where appropriate.
- Test contrast and focus behavior.

## 7. Design tokens

The implementation will define tokens for:

- Typography
- Spacing
- Corner radii
- Elevation
- Icon sizes
- Motion durations
- Motion easing/spring behavior
- Semantic colors

Tokens must be consumed by components rather than hard-coded throughout feature screens.
