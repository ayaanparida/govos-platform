package com.govos.srh.ai.job;

import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.SearchEmbedding;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.ai.VectorIndexService;
import com.govos.srh.config.SearchProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingGenerationService {

    private final EmbeddingProvider embeddingProvider;
    private final VectorIndexService vectorIndexService;
    private final SearchProperties searchProperties;
    private final Map<UUID, EmbeddingGenerationJob> jobs = new ConcurrentHashMap<>();

    public EmbeddingGenerationService(
            EmbeddingProvider embeddingProvider,
            VectorIndexService vectorIndexService,
            SearchProperties searchProperties) {
        this.embeddingProvider = embeddingProvider;
        this.vectorIndexService = vectorIndexService;
        this.searchProperties = searchProperties;
    }

    public EmbeddingGenerationJob startJob(List<EmbeddingDocumentTarget> targets) {
        UUID jobId = UUID.randomUUID();
        int version = searchProperties.getSemantic().getEmbeddingVersion();
        EmbeddingGenerationJob job = new EmbeddingGenerationJob(jobId, version);
        job.setTotalDocuments(targets != null ? targets.size() : 0);
        job.setStatus(EmbeddingGenerationJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        jobs.put(jobId, job);

        if (targets == null || targets.isEmpty()) {
            job.setStatus(EmbeddingGenerationJobStatus.COMPLETED);
            completeJob(job);
            return job;
        }

        int batchSize = Math.max(1, searchProperties.getSemantic().getEmbeddingBatchSize());
        int maxRetries = Math.max(0, searchProperties.getSemantic().getEmbeddingMaxRetries());
        List<EmbeddingDocumentTarget> pending = new ArrayList<>(targets);

        while (!pending.isEmpty() && job.getStatus() == EmbeddingGenerationJobStatus.RUNNING) {
            List<EmbeddingDocumentTarget> batch = pending.subList(0, Math.min(batchSize, pending.size()));
            processBatch(job, batch, maxRetries);
            pending.removeAll(batch);
        }

        if (job.getFailedDocuments() > 0 && job.getProcessedDocuments() == 0) {
            job.setStatus(EmbeddingGenerationJobStatus.FAILED);
        } else if (job.getFailedDocuments() > 0) {
            job.setStatus(EmbeddingGenerationJobStatus.COMPLETED);
        } else {
            job.setStatus(EmbeddingGenerationJobStatus.COMPLETED);
        }
        completeJob(job);
        return job;
    }

    public EmbeddingGenerationJob resumeJob(UUID jobId, List<EmbeddingDocumentTarget> remainingTargets) {
        EmbeddingGenerationJob existing = jobs.get(jobId);
        if (existing == null) {
            throw new SemanticSearchException("Embedding generation job not found: " + jobId);
        }
        if (existing.getStatus() != EmbeddingGenerationJobStatus.FAILED
                && existing.getStatus() != EmbeddingGenerationJobStatus.CANCELLED) {
            throw new SemanticSearchException("Embedding generation job cannot be resumed from status: "
                    + existing.getStatus());
        }
        existing.setStatus(EmbeddingGenerationJobStatus.RUNNING);
        existing.setCompletedAt(null);
        return startJob(remainingTargets);
    }

    public EmbeddingGenerationJob getJob(UUID jobId) {
        EmbeddingGenerationJob job = jobs.get(jobId);
        if (job == null) {
            throw new SemanticSearchException("Embedding generation job not found: " + jobId);
        }
        return job;
    }

    private void processBatch(
            EmbeddingGenerationJob job,
            List<EmbeddingDocumentTarget> batch,
            int maxRetries) {
        List<String> texts = batch.stream().map(EmbeddingDocumentTarget::text).toList();
        int attempt = 0;
        while (true) {
            try {
                List<float[]> vectors = embeddingProvider.generateEmbeddings(texts);
                List<SearchEmbedding> embeddings = new ArrayList<>(batch.size());
                Instant now = Instant.now();
                for (int i = 0; i < batch.size(); i++) {
                    EmbeddingDocumentTarget target = batch.get(i);
                    SearchEmbedding embedding = new SearchEmbedding();
                    embedding.setEmbeddingId(UUID.randomUUID());
                    embedding.setReferenceId(target.referenceId());
                    embedding.setOrganizationId(target.organizationId());
                    embedding.setEntityType(target.entityType());
                    embedding.setEmbeddingVersion(job.getTargetVersion());
                    embedding.setVectorDimension(embeddingProvider.embeddingDimension());
                    embedding.setVector(vectors.get(i));
                    embedding.setCreatedDate(now);
                    embedding.setUpdatedDate(now);
                    embeddings.add(embedding);
                }
                vectorIndexService.indexEmbeddings(embeddings);
                for (int i = 0; i < batch.size(); i++) {
                    job.incrementProcessed();
                }
                return;
            } catch (RuntimeException ex) {
                attempt++;
                job.setLastError(ex.getClass().getSimpleName());
                if (attempt > maxRetries) {
                    for (int i = 0; i < batch.size(); i++) {
                        job.incrementFailed();
                    }
                    return;
                }
            }
        }
    }

    private void completeJob(EmbeddingGenerationJob job) {
        job.setCompletedAt(Instant.now());
    }
}
