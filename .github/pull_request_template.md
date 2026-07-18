## Quality Gates

Complete before requesting review. See [Merge Quality Gates](https://github.com/ayaanparida/govos-architecture/blob/develop/docs/06-engineering/merge-quality-gates.md).

- [ ] **Architecture** — Package, naming, and module boundaries reviewed
- [ ] **Code Quality** — `.\mvnw.cmd -pl govos-api -am clean verify` passes from `backend/`; no new compiler warnings
- [ ] **Testing** — Unit tests for new/changed services; ≥ 80% service-layer coverage on touched code
- [ ] **Flyway** — New migration only (no edits to merged migrations); version and naming correct; tested on clean DB
- [ ] **Documentation** — `govos-domain/{ctx}/README.md` and architecture docs updated as needed
- [ ] **Dependencies** — No circular Maven module deps; new third-party libraries justified in Summary

## Summary

<!-- What changed and why (1–3 bullets) -->

## Bounded Context

<!-- e.g. NTF, ORG, infrastructure — or "cross-cutting" -->

## Flyway

<!-- Version applied (e.g. V1.5.1) or "N/A" -->

## Manual Rollback (if schema changed)

<!-- Forward-only Flyway — document emergency rollback SQL or "N/A" -->

## Test Plan

- [ ] Build verified locally
- [ ] Flyway migration applied (if applicable)
- [ ] Unit tests added/passing
