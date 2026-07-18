package com.govos.srh.ai.migration;

import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.ai.SearchEmbedding;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.ai.VectorIndexService;
import com.govos.srh.ai.job.EmbeddingDocumentTarget;
import com.govos.srh.ai.job.EmbeddingGenerationJob;
import com.govos.srh.ai.job.EmbeddingGenerationService;
import com.govos.srh.ai.vector.OpenSearchVectorIndexService;
import com.govos.srh.config.SearchProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingMigrationService {

    private final EmbeddingProvider embeddingProvider;
    private final VectorIndexService vectorIndexService;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final SearchProperties searchProperties;
    private final Map<UUID, EmbeddingMigrationStatus> migrations = new ConcurrentHashMap<>();

    public EmbeddingMigrationService(
            EmbeddingProvider embeddingProvider,
            VectorIndexService vectorIndexService,
            EmbeddingGenerationService embeddingGenerationService,
            SearchProperties searchProperties) {
        this.embeddingProvider = embeddingProvider;
        this.vectorIndexService = vectorIndexService;
        this.embeddingGenerationService = embeddingGenerationService;
        this.searchProperties = searchProperties;
    }

    public EmbeddingMigrationStatus startMigration(int targetVersion, List<EmbeddingDocumentTarget> documents) {
        UUID migrationId = UUID.randomUUID();
        int currentVersion = searchProperties.getSemantic().getEmbeddingVersion();
        EmbeddingMigrationStatus status = new EmbeddingMigrationStatus(
                migrationId,
                currentVersion,
                targetVersion,
                EmbeddingMigrationPhase.RUNNING,
                Instant.now(),
                null,
                0,
                0);
        migrations.put(migrationId, status);

        try {
            EmbeddingGenerationJob job = embeddingGenerationService.startJob(documents);
            status.setProcessedDocuments(job.getProcessedDocuments());
            status.setFailedDocuments(job.getFailedDocuments());
            if (job.getFailedDocuments() == 0 && job.getProcessedDocuments() > 0) {
                searchProperties.getSemantic().setEmbeddingVersion(targetVersion);
                status.setPhase(EmbeddingMigrationPhase.COMPLETED);
            } else if (job.getFailedDocuments() > 0) {
                status.setPhase(EmbeddingMigrationPhase.FAILED);
            } else {
                status.setPhase(EmbeddingMigrationPhase.COMPLETED);
            }
        } catch (RuntimeException ex) {
            status.setPhase(EmbeddingMigrationPhase.FAILED);
            status.setLastError(ex.getClass().getSimpleName());
        }
        status.setCompletedAt(Instant.now());
        return status;
    }

    public EmbeddingMigrationStatus rollbackMigration(UUID migrationId, List<EmbeddingDocumentTarget> documents) {
        EmbeddingMigrationStatus status = migrations.get(migrationId);
        if (status == null) {
            throw new SemanticSearchException("Embedding migration not found: " + migrationId);
        }
        int versionToRemove = status.targetVersion();
        if (vectorIndexService instanceof OpenSearchVectorIndexService openSearchVectorIndexService) {
            for (EmbeddingDocumentTarget document : documents) {
                openSearchVectorIndexService.deleteEmbedding(document.referenceId(), versionToRemove);
            }
        }
        searchProperties.getSemantic().setEmbeddingVersion(status.sourceVersion());
        EmbeddingMigrationStatus rolledBack = new EmbeddingMigrationStatus(
                migrationId,
                status.sourceVersion(),
                versionToRemove,
                EmbeddingMigrationPhase.ROLLED_BACK,
                status.startedAt(),
                Instant.now(),
                status.processedDocuments(),
                status.failedDocuments());
        migrations.put(migrationId, rolledBack);
        return rolledBack;
    }

    public EmbeddingMigrationStatus rebuildVectors(List<EmbeddingDocumentTarget> documents) {
        int version = searchProperties.getSemantic().getEmbeddingVersion();
        List<SearchEmbedding> embeddings = new ArrayList<>();
        Instant now = Instant.now();
        List<float[]> vectors = embeddingProvider.generateEmbeddings(
                documents.stream().map(EmbeddingDocumentTarget::text).toList());
        for (int i = 0; i < documents.size(); i++) {
            EmbeddingDocumentTarget target = documents.get(i);
            SearchEmbedding embedding = new SearchEmbedding();
            embedding.setEmbeddingId(UUID.randomUUID());
            embedding.setReferenceId(target.referenceId());
            embedding.setOrganizationId(target.organizationId());
            embedding.setEntityType(target.entityType());
            embedding.setEmbeddingVersion(version);
            embedding.setVectorDimension(embeddingProvider.embeddingDimension());
            embedding.setVector(vectors.get(i));
            embedding.setCreatedDate(now);
            embedding.setUpdatedDate(now);
            embeddings.add(embedding);
        }
        vectorIndexService.indexEmbeddings(embeddings);
        return new EmbeddingMigrationStatus(
                UUID.randomUUID(),
                version,
                version,
                EmbeddingMigrationPhase.REINDEXED,
                Instant.now(),
                Instant.now(),
                documents.size(),
                0);
    }

    public EmbeddingMigrationStatus getMigration(UUID migrationId) {
        EmbeddingMigrationStatus status = migrations.get(migrationId);
        if (status == null) {
            throw new SemanticSearchException("Embedding migration not found: " + migrationId);
        }
        return status;
    }
}
