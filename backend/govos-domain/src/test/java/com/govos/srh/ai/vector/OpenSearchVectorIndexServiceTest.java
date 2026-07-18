package com.govos.srh.ai.vector;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.production.SearchMetricsRecorder;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.transport.endpoints.BooleanResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenSearchVectorIndexServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;
    @Mock
    private SearchMetricsRecorder metricsRecorder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenSearchIndicesClient indicesClient;

    private OpenSearchVectorIndexService service;

    @BeforeEach
    void setUp() throws Exception {
        SearchProperties properties = new SearchProperties();
        properties.getSemantic().setVectorIndexName("govos-vector-index");
        properties.getSemantic().setVectorDimension(3);
        properties.getSemantic().setEmbeddingVersion(1);

        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(ExistsRequest.class)))
                .thenReturn(new BooleanResponse(true));

        service = new OpenSearchVectorIndexService(openSearchClient, properties, metricsRecorder);
    }

    @Test
    void shouldReturnEmptyResultsWhenIndexMissing() throws Exception {
        when(indicesClient.exists(any(ExistsRequest.class)))
                .thenReturn(new BooleanResponse(false));

        assertThat(service.search(UUID.randomUUID(), new float[] {0.1f, 0.2f, 0.3f}, 5)).isEmpty();
    }

    @Test
    void shouldCountIndexedVectors() throws Exception {
        CountResponse countResponse = CountResponse.of(r -> r.count(42L).shards(s -> s.total(1).successful(1).failed(0)));
        when(openSearchClient.count(any(CountRequest.class))).thenReturn(countResponse);

        assertThat(service.count()).isEqualTo(42L);
    }

    @Test
    void shouldReportHealth() {
        assertThat(service.health()).isEqualTo(com.govos.srh.ai.EmbeddingHealthStatus.UP);
    }
}
