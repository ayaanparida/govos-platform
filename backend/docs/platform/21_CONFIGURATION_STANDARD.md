# GPS-001 — 21 Configuration Standard

---

## 1. Property Namespace

All module configuration under:

```
govos.{context}.{property-group}.{property}
```

Examples:
- `govos.search.host`
- `govos.search.semantic.enabled`
- `govos.platform.release`

---

## 2. Binding

```java
@ConfigurationProperties(prefix = "govos.search")
public class SearchProperties {
    private String host = "localhost";
    private SemanticSearchProperties semantic = new SemanticSearchProperties();
}
```

- Nested POJOs for groups
- Enable via `@EnableConfigurationProperties`
- Manual getters/setters (consistent with entity standard)

---

## 3. Environment Overrides

YAML supports env substitution:

```yaml
govos.search.host: ${GOVOS_SEARCH_HOST:localhost}
```

Convention: `GOVOS_{CONTEXT}_{PROPERTY}` uppercase snake case.

---

## 4. Profiles

| Profile | Usage |
|---------|-------|
| `local` | Developer workstation |
| `prod` | Production deployment |
| `test` | Test execution (if needed) |

Activate: `SPRING_PROFILES_ACTIVE=prod`

Profile-specific files: `application-prod.yml`

---

## 5. Secrets

| Do | Don't |
|----|-------|
| `${ENV_VAR}` placeholders | Hardcode secrets in yaml |
| K8s Secrets / vault injection | Commit `.env` with secrets |
| Document required env vars in module README | Assume defaults in prod |

---

## 6. Defaults

- Every property has a safe default for local development
- Production-sensitive defaults (mock provider, ssl false) documented with override requirement
- Fail fast on missing required prod secrets where possible

---

## 7. Configuration Documentation

Each module README or CONFIGURATION_REFERENCE must list:

- Property name
- Default value
- Environment override
- Required/optional in production
- Production recommendation

---

## 8. Feature Flags

Boolean `enabled` properties gate optional features:

```yaml
govos.search.semantic.enabled: false
govos.search.scheduler.enabled: true
govos.search.observation.enabled: true
```

Use `@ConditionalOnProperty` for optional infrastructure beans.

---

## 9. Prohibited

- System property overrides without documentation
- Multiple prefix styles for same module
- Configuration classes in `govos-api` for domain concerns (prefer domain `config` package)
