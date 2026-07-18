package com.govos.srh.ai.job;

import com.govos.srh.ai.InMemoryVectorIndexService;
import com.govos.srh.ai.MockEmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingGenerationServiceTest {

    private EmbeddingGenerationService service;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setEmbeddingBatchSize(2);
        properties.getSemantic().setEmbeddingMaxRetries(1);
        properties.getSemantic().setEmbeddingVersion(1);
        service = new EmbeddingGenerationService(
                new MockEmbeddingProvider(),
                new InMemoryVectorIndexService(),
                properties);
    }

    @Test
    void shouldGenerateEmbeddingsInBatches() {
        UUID ref1 = UUID.randomUUID();
        UUID ref2 = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        EmbeddingGenerationJob job = service.startJob(List.of(
                new EmbeddingDocumentTarget(ref1, orgId, "COMPLAINT", "water leak"),
                new EmbeddingDocumentTarget(ref2, orgId, "COMPLAINT", "road repair")));

        assertThat(job.getStatus()).isEqualTo(EmbeddingGenerationJobStatus.COMPLETED);
        assertThat(job.getProcessedDocuments()).isEqualTo(2);
        assertThat(job.getFailedDocuments()).isZero();
    }

    @Test
    void shouldCompleteEmptyJob() {
        EmbeddingGenerationJob job = service.startJob(List.of());

        assertThat(job.getStatus()).isEqualTo(EmbeddingGenerationJobStatus.COMPLETED);
        assertThat(job.getProcessedDocuments()).isZero();
    }
}
