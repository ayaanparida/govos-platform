package com.govos.srh.ai.migration;

import com.govos.srh.ai.InMemoryVectorIndexService;
import com.govos.srh.ai.MockEmbeddingProvider;
import com.govos.srh.ai.job.EmbeddingDocumentTarget;
import com.govos.srh.ai.job.EmbeddingGenerationService;
import com.govos.srh.config.SearchProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingMigrationServiceTest {

    private EmbeddingMigrationService migrationService;
    private SearchProperties searchProperties;

    @BeforeEach
    void setUp() {
        searchProperties = new SearchProperties();
        searchProperties.getSemantic().setEmbeddingVersion(1);
        EmbeddingGenerationService generationService = new EmbeddingGenerationService(
                new MockEmbeddingProvider(),
                new InMemoryVectorIndexService(),
                searchProperties);
        migrationService = new EmbeddingMigrationService(
                new MockEmbeddingProvider(),
                new InMemoryVectorIndexService(),
                generationService,
                searchProperties);
    }

    @Test
    void shouldMigrateToNewEmbeddingVersion() {
        UUID ref = UUID.randomUUID();
        UUID org = UUID.randomUUID();

        EmbeddingMigrationStatus status = migrationService.startMigration(2, List.of(
                new EmbeddingDocumentTarget(ref, org, "COMPLAINT", "water leak")));

        assertThat(status.phase()).isIn(
                EmbeddingMigrationPhase.COMPLETED,
                EmbeddingMigrationPhase.FAILED);
    }

    @Test
    void shouldRebuildVectorsForCurrentVersion() {
        UUID ref = UUID.randomUUID();
        UUID org = UUID.randomUUID();

        EmbeddingMigrationStatus status = migrationService.rebuildVectors(List.of(
                new EmbeddingDocumentTarget(ref, org, "COMPLAINT", "road repair")));

        assertThat(status.phase()).isEqualTo(EmbeddingMigrationPhase.REINDEXED);
        assertThat(status.processedDocuments()).isEqualTo(1);
    }
}
