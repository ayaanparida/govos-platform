# GPS-001 — 26 Release Standard

---

## 1. Versioning

| Artifact | Scheme | Example |
|----------|--------|---------|
| Platform API | SemVer SNAPSHOT → release | `0.1.0-SNAPSHOT` |
| Bounded context | Milestone tags | SRH-001 … SRH-020 |
| Context release | SemVer | SRH v1.0.0 |
| GPS standards | Document version | GPS-001 v1.0.0 |
| Flyway | `V{major}_{minor}_{patch}` | V2_1_0 |

---

## 2. Milestones

Each bounded context delivers numbered sprints:

```
{CTX}-{nnn} — {Title}
```

Examples: `SRH-019 Scheduler`, `CMP-015 Search Integration`

Milestones documented in module README with scope, architecture constraints, and verification commands.

---

## 3. Release Types

| Type | Description |
|------|-------------|
| **Feature sprint** | New capability (SRH-019) |
| **Certification release** | Documentation + validation only (SRH Release-1.0) |
| **Platform standard** | Engineering baseline (GPS-001) |
| **Hotfix** | Critical bug fix — minimal scope |

---

## 4. Release Checklist

- [ ] All tests pass: `mvn -pl govos-api -am test`
- [ ] JaCoCo gates pass: `mvn verify`
- [ ] Flyway migration reviewed (if schema change)
- [ ] README updated with sprint section
- [ ] Breaking changes documented (none allowed in patch)
- [ ] Configuration new properties documented
- [ ] Security review for new endpoints/permissions
- [ ] OpenAPI spec regenerated if API changes
- [ ] Certification docs updated (for major releases)

---

## 5. Certification Process

For platform module certification (e.g. SRH v1.0.0):

1. Feature complete — all milestones delivered
2. Architecture validation report
3. Security review
4. Performance sizing documentation
5. Operational runbooks
6. Compatibility matrix
7. Release notes
8. **Code freeze** — documentation-only changes

---

## 6. Backward Compatibility

| Change | Allowed in patch? |
|--------|-------------------|
| Additive REST endpoint | Yes |
| New optional config property | Yes |
| New permission | Yes (with IDM update) |
| Remove endpoint | No — requires v2 |
| Rename config property | No — deprecate first |
| Entity column removal | No — Flyway forward only |

---

## 7. Git Tagging

Certified releases tagged:

```
srh-v1.0.0
gps-001-v1.0.0
govos-platform-0.1.0
```

Tag after certification approval and BUILD SUCCESS.

---

## 8. Release Notes Template

```markdown
# {Module} Release Notes — v{X.Y.Z}
## Summary
## Milestones included
## Breaking changes
## Upgrade notes
## Verification
```

---

## 9. Documentation Freeze

Certification releases (Release-1.0 pattern):
- No production code changes
- Documentation and validation artifacts only
- Maven test must still pass

---

## 10. Prohibited

- Releasing without test verification
- Undocumented breaking API changes
- Skipping Flyway for schema changes
- Mixing unrelated module changes in one certification release
