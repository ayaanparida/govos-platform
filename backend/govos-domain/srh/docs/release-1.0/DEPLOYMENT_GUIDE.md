# SRH v1.0.0 — Deployment Guide

---

## 1. Architecture Components

| Component | Deployment | Notes |
|-----------|------------|-------|
| govos-api | Container / VM | Includes SRH module |
| PostgreSQL | Managed or self-hosted | Metadata (`srh_*` tables) |
| OpenSearch | Cluster (3+ nodes prod) | Full-text + kNN |
| OTLP Collector | Optional | Observability export |
| Prometheus | Optional | Metrics scrape |

---

## 2. Docker Deployment

Build platform API artifact:

```bash
cd govos-platform/backend
mvn -pl govos-api -am package -DskipTests
```

Example Dockerfile pattern (platform-level):

```dockerfile
FROM eclipse-temurin:21-jre
COPY govos-api/target/govos-api-*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx2g"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Required environment variables:**

```bash
GOVOS_SEARCH_HOST=opensearch.internal
GOVOS_SEARCH_PORT=9200
GOVOS_SEARCH_USERNAME=srh_service
GOVOS_SEARCH_PASSWORD=<secret>
GOVOS_SEARCH_SSL=true
SPRING_PROFILES_ACTIVE=prod
```

---

## 3. Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: govos-api
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: govos-api
          image: govos/platform-api:1.0.0
          ports:
            - containerPort: 8080
          envFrom:
            - secretRef:
                name: govos-search-secrets
            - configMapRef:
                name: govos-search-config
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
          resources:
            requests:
              cpu: 500m
              memory: 1Gi
            limits:
              cpu: 2
              memory: 2Gi
```

**OpenSearch cluster:** Deploy separately (ECK, OpenSearch Operator, or managed service). SRH connects via service URL.

---

## 4. OpenSearch Cluster Requirements

| Scale | Nodes | Heap | Storage |
|-------|-------|------|---------|
| Dev | 1 | 2 GB | 20 GB |
| 100K docs | 3 | 4 GB each | 50 GB |
| 1M docs | 3 | 8 GB each | 200 GB |
| 10M docs | 5+ | 16 GB each | 1 TB+ |

Enable kNN plugin for semantic search. Create vector index matching `semantic.vector-index-name`.

---

## 5. Rolling Upgrade

1. Deploy new API version to one pod
2. Verify `/actuator/health` and `/admin/health/operational`
3. Rolling update remaining pods
4. No OpenSearch downtime required for API upgrade
5. Run smoke queries post-upgrade

---

## 6. Blue-Green Deployment

1. Deploy green API stack pointing to same OpenSearch + PostgreSQL
2. Run integration smoke tests against green
3. Switch load balancer to green
4. Keep blue for rollback window (24h)
5. SRH state in PostgreSQL/OpenSearch is shared — both stacks can coexist briefly

---

## 7. Disaster Recovery

### Backup
- **PostgreSQL:** Daily snapshot of `srh_*` tables (platform backup policy)
- **OpenSearch:** Snapshot repository (S3/GCS) — daily incremental
- **Config:** Git-managed `application-prod.yml` + secrets in vault

### Restore
1. Restore PostgreSQL from snapshot
2. Restore OpenSearch snapshot to new cluster
3. Update `GOVOS_SEARCH_HOST` to restored cluster
4. Trigger full reindex if OpenSearch restore unavailable: `POST /admin/reindex-all`
5. Validate document counts per index

**RPO:** 24 hours (default backup cadence)  
**RTO:** 4 hours (target with runbooks)

---

## 8. Flyway Migrations

SRH schema: `V2_1_0__search.sql` in `govos-infrastructure`. Applied automatically on startup. Do not modify post v1.0 freeze.

---

## 9. Profile Activation

| Profile | Use |
|---------|-----|
| `local` | Developer workstation |
| `prod` | Production — semantic OpenSearch vectors, cache warm, Prometheus |

```bash
SPRING_PROFILES_ACTIVE=prod
```
