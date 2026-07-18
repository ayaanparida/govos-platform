# GPS-001 — 24 AI Integration Standard

---

## 1. Provider Abstraction

All AI/embedding access through **provider interfaces** in domain layer:

```java
public interface EmbeddingProvider {
    SearchEmbedding embed(String text);
    EmbeddingHealthStatus health();
}
```

Implementations: `mock`, `openai`, `azure-openai`, `ollama` — selected via factory + configuration.

---

## 2. Configuration-Driven Selection

```yaml
govos.search.semantic:
  provider: openai
  openai:
    api-key: ${GOVOS_SEARCH_OPENAI_API_KEY}
```

- Factory selects provider at startup
- Misconfiguration falls back to mock with WARN log (never log key)
- No vendor lock-in — swap via config

---

## 3. Embedding Versioning

- `embedding-version` integer in configuration
- Increment on model change
- Migration/re-generation jobs for version upgrades
- Store version on indexed vectors

---

## 4. Vector Stores

| Store | Usage |
|-------|-------|
| `memory` | Development/test only |
| `opensearch` | Production kNN index |

Abstract via `VectorIndexService` — products never access vector store directly.

---

## 5. Semantic Search

- Semantic/hybrid search owned by platform module (SRH)
- Gated by `semantic.enabled=true`
- Hybrid ranking: configurable keyword/vector weights
- Products call `SearchApplicationService.semanticSearch()` / `hybridSearch()`

---

## 6. Model Upgrades

Process:
1. Configure new model/provider
2. Increment `embedding-version`
3. Run embedding generation/migration job
4. Validate hybrid search quality
5. Decommission old embeddings

Document in module operations guide.

---

## 7. Prompt Safety

GovOS v1.0 SRH uses **embedding APIs only** (no LLM prompt generation in search path).

If future modules add LLM prompts:
- Sanitize user input before external calls
- Never send PII without redaction policy
- Log prompt metadata only — not full prompt text
- Rate limit external API calls

---

## 8. Caching

Embedding cache (Caffeine):
- Key: hash of text + provider + version
- TTL and max entries configurable
- Never cache across embedding versions

---

## 9. Metrics & Health

| Metric | Tag |
|--------|-----|
| `embedding.requests` | provider |
| `embedding.duration` | provider |
| `embedding.errors` | provider |
| `provider.health` | provider, status |

Admin endpoint: provider info (no secrets).

---

## 10. Prohibited

- Products calling OpenAI/Azure/Ollama directly
- Storing API keys in database or git
- Logging embedding vectors or source text
- Hardcoded model names without configuration

---

## 11. Reference

SRH-016 (semantic search), SRH-018 (production providers) — certified v1.0.
