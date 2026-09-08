## CardWise change

### Change type
- [ ] Feature
- [ ] Bug fix
- [ ] Refactor
- [ ] Design/UX
- [ ] Engine/domain logic
- [ ] Test-only
- [ ] Documentation

### Project memory
- [ ] `docs/cardwise/KNOWLEDGE_GRAPH.yaml` updated if feature/engine/screen/flow/status changed
- [ ] `docs/cardwise/SCREEN_MATRIX.md` updated if UI/state/interaction changed
- [ ] `docs/cardwise/ENGINE_CATALOG.md` updated if business-engine responsibility changed
- [ ] `docs/cardwise/AI_CONTEXT.md` / `docs/STATUS.md` updated if current next steps or baseline changed
- [ ] No prototype/mock data has been promoted to production truth without a provider/data decision

### Verification
- [ ] Unit tests
- [ ] Compose/instrumentation tests
- [ ] Android build
- [ ] APK/manual validation where applicable
- [ ] CI verified against the exact commit

### Safety checks
- [ ] Launcher logo was not changed unless explicitly authorized
- [ ] No UPI PIN/banking password/PAN/CVV or equivalent credential is stored
- [ ] Payment handoff still requires explicit user action
- [ ] QR input remains treated as untrusted
- [ ] Business logic remains outside Compose UI
