# SRH v1.0.0 — Configuration Reference

Complete audit of all `govos.search.*` properties.

**Legend:** R = Required in production | O = Optional | E = Environment override supported

---

## Top-Level Connection

| Property | Default | E | Prod recommendation |
|----------|---------|---|---------------------|
| `govos.search.host` | `localhost` | `GOVOS_SEARCH_HOST` | OpenSearch cluster endpoint |
| `govos.search.port` | `9200` | `GOVOS_SEARCH_PORT` | `9200` or `443` (TLS) |
| `govos.search.username` | — | `GOVOS_SEARCH_USERNAME` | R — service account |
| `govos.search.password` | — | `GOVOS_SEARCH_PASSWORD` | R — secret manager |
| `govos.search.ssl` | `false` | `GOVOS_SEARCH_SSL` | R — `true` in production |
| `govos.search.bulk-batch-size` | `500` | — | 500–1000 |
| `govos.search.default-page-size` | `20` | — | 20 |
| `govos.search.max-page-size` | `100` | — | 100 |
| `govos.search.query-timeout-ms` | `5000` | — | 5000–10000 |

---

## Semantic Search (`govos.search.semantic.*`)

| Property | Default | E | Prod recommendation |
|----------|---------|---|---------------------|
| `enabled` | `false` | — | `true` if semantic needed |
| `keyword-weight` | `0.70` | — | Tune per use case |
| `vector-weight` | `0.30` | — | Tune per use case |
| `provider` | `mock` | — | `openai`, `azure-openai`, or `ollama` |
| `top-k` | `20` | — | 20–50 |
| `vector-store` | `memory` | — | **`opensearch`** |
| `vector-index-name` | `govos-vector-index` | — | Keep default |
| `embedding-version` | `1` | — | Increment on model change |
| `embedding-batch-size` | `50` | — | 50 |
| `embedding-max-retries` | `3` | — | 3 |
| `vector-dimension` | `384` | — | Match provider (1536 OpenAI) |

### OpenAI (`semantic.openai.*`)

| Property | Default | E |
|----------|---------|---|
| `api-key` | — | `GOVOS_SEARCH_OPENAI_API_KEY` |
| `model` | `text-embedding-3-small` | — |
| `base-url` | OpenAI default | `GOVOS_SEARCH_OPENAI_BASE_URL` |
| `dimension` | `1536` | — |

### Azure OpenAI (`semantic.azure.*`)

| Property | Default | E |
|----------|---------|---|
| `endpoint` | — | `GOVOS_SEARCH_AZURE_ENDPOINT` |
| `api-key` | — | `GOVOS_SEARCH_AZURE_API_KEY` |
| `deployment` | — | `GOVOS_SEARCH_AZURE_DEPLOYMENT` |
| `api-version` | `2023-05-15` | — |
| `dimension` | `1536` | — |

### Ollama (`semantic.ollama.*`)

| Property | Default | E |
|----------|---------|---|
| `base-url` | `http://localhost:11434` | `GOVOS_SEARCH_OLLAMA_BASE_URL` |
| `model` | `nomic-embed-text` | — |
| `dimension` | `768` | — |

### Embedding Cache (`semantic.embedding-cache.*`)

| Property | Default | Prod |
|----------|---------|------|
| `enabled` | `true` | `true` |
| `ttl-seconds` | `3600` | 3600 |
| `max-entries` | `5000` | 10000+ |

---

## Resilience (`govos.search.resilience.*`)

| Property | Default | Prod recommendation |
|----------|---------|---------------------|
| `max-retries` | `3` | 3 |
| `initial-backoff-ms` | `100` | 100 |
| `max-backoff-ms` | `2000` | 2000 |
| `backoff-multiplier` | `2.0` | 2.0 |
| `operation-timeout-ms` | `5000` | 10000 |
| `graceful-degradation` | `true` | **`true`** |

---

## Cache (`govos.search.cache.*`)

| Property | Default | Prod |
|----------|---------|------|
| `enabled` | `true` | `true` |
| `ttl-seconds` | `60` | 120–300 |
| `max-entries` | `1000` | 5000+ |
| `warm-on-startup` | `false` | **`true`** |

---

## Connection Pool (`govos.search.pool.*`)

| Property | Default | Prod |
|----------|---------|------|
| `max-connections` | `50` | **100** |
| `max-connections-per-route` | `20` | **50** |
| `connection-timeout-ms` | `5000` | 5000 |
| `socket-timeout-ms` | `30000` | 30000 |
| `compression` | `true` | `true` |

---

## Guards (`govos.search.guard.*`)

| Property | Default | Prod |
|----------|---------|------|
| `max-result-window` | `10000` | 10000 |
| `max-deep-pagination-offset` | `10000` | 10000 |

---

## Metrics (`govos.search.metrics.*`)

| Property | Default | Prod |
|----------|---------|------|
| `enabled` | `true` | **`true`** |

---

## Scheduler (`govos.search.scheduler.*`)

| Property | Default | Prod |
|----------|---------|------|
| `enabled` | `true` | `true` |
| `reindex-cron` | `0 0 2 * * *` | Daily 02:00 |
| `incremental-reindex-cron` | `0 0 */6 * * *` | Every 6 hours |
| `embedding-cron` | `0 30 3 * * *` | Daily 03:30 |
| `cleanup-cron` | `0 0 4 * * *` | Daily 04:00 |
| `health-cron` | `0 */15 * * * *` | Every 15 min |
| `statistics-cron` | `0 5 * * * *` | Hourly :05 |
| `max-retries` | `3` | 3 |
| `initial-backoff-ms` | `1000` | 1000 |
| `max-backoff-ms` | `30000` | 30000 |
| `backoff-multiplier` | `2.0` | 2.0 |
| `query-history-retention-days` | `90` | 90 |
| `history-max-entries` | `500` | 500 |

---

## Observation (`govos.search.observation.*`)

| Property | Default | E | Prod |
|----------|---------|---|------|
| `enabled` | `true` | `GOVOS_SEARCH_OBSERVATION_ENABLED` | `true` |
| `exporter` | `otlp` | — | `otlp` |
| `otlp-endpoint` | `http://localhost:4317` | `GOVOS_SEARCH_OBSERVATION_OTLP_ENDPOINT` | Collector URL |
| `sample-rate` | `1.0` | — | 0.1–1.0 |
| `log-spans` | `false` | — | `false` |
| `log-events` | `false` | — | `false` |
| `trace-history-max-entries` | `500` | — | 500 |

---

## Example Production Block

```yaml
govos.search:
  host: ${GOVOS_SEARCH_HOST}
  port: 9200
  username: ${GOVOS_SEARCH_USERNAME}
  password: ${GOVOS_SEARCH_PASSWORD}
  ssl: true
  semantic:
    enabled: true
    provider: openai
    vector-store: opensearch
    openai:
      api-key: ${GOVOS_SEARCH_OPENAI_API_KEY}
  cache:
    warm-on-startup: true
  pool:
    max-connections: 100
  scheduler:
    enabled: true
  observation:
    enabled: true
    otlp-endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT}
    sample-rate: 0.25
```
