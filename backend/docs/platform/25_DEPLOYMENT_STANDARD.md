# GPS-001 — 25 Deployment Standard

---

## 1. Deployable Artifact

Single Spring Boot JAR:

```
govos-api/target/govos-api-*.jar
```

Built with:

```bash
mvn -pl govos-api -am package
```

---

## 2. Runtime Requirements

| Requirement | Specification |
|-------------|---------------|
| JRE | Java 21 (Eclipse Temurin recommended) |
| Memory | Min 1 GB heap (dev); 2+ GB (prod) |
| Database | PostgreSQL 15+ reachable |
| External deps | Per module (OpenSearch for SRH, etc.) |

---

## 3. Docker

Recommended base: `eclipse-temurin:21-jre`

```dockerfile
FROM eclipse-temurin:21-jre
COPY govos-api/target/govos-api-*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx2g"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

- Non-root user in production
- Health check: `GET /actuator/health/liveness`

---

## 4. Kubernetes

| Resource | Guidance |
|----------|----------|
| Deployment | 2+ replicas prod |
| Service | ClusterIP → ingress |
| ConfigMap | Non-secret config |
| Secret | Credentials, API keys |
| Probes | liveness + readiness via actuator |
| Resources | requests/limits per PERFORMANCE guide |

---

## 5. Helm

No official GovOS Helm chart in GPS-001 baseline. Platform teams should:

- Parameterize image tag, replicas, env vars
- Mount secrets for DB, JWT, provider keys
- Configure ingress TLS

---

## 6. Configuration in Deployment

- `SPRING_PROFILES_ACTIVE=prod`
- All secrets via environment injection
- Feature flags via ConfigMap

---

## 7. Health Checks

| Probe | Path |
|-------|------|
| Liveness | `/actuator/health/liveness` |
| Readiness | `/actuator/health/readiness` |
| Module health | `/api/v1/{ctx}/admin/health` (authenticated) |

---

## 8. Rolling Upgrades

1. Deploy new version to subset of pods
2. Wait for readiness probe success
3. Rolling update remaining pods
4. Run smoke tests against `/actuator/health` and key API endpoints
5. Monitor error rates in Prometheus

Zero-downtime assumes backward-compatible schema (Flyway forward migrations applied first).

---

## 9. Blue-Green (Optional)

- Two deployments share PostgreSQL + OpenSearch
- Switch service selector after green validation
- Rollback by switching selector back

---

## 10. Prohibited

- Deploying without Flyway migration review
- Single replica in production without ADR
- Running as root in container
- Exposing actuator unauthenticated in prod
