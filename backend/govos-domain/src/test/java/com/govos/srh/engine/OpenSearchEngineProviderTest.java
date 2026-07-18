package com.govos.srh.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.exception.SearchEngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.client.opensearch.cluster.HealthResponse;
import org.opensearch.client.opensearch.cluster.OpenSearchClusterClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest;
import org.opensearch.client.opensearch.indices.UpdateAliasesResponse;
import org.opensearch.client.transport.endpoints.BooleanResponse;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenSearchEngineProviderTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private OpenSearchIndicesClient indicesClient;

    @Mock
    private OpenSearchClusterClient clusterClient;

    private OpenSearchEngineProvider provider;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.setBulkBatchSize(2);
        provider = new OpenSearchEngineProvider(openSearchClient, properties, new ObjectMapper());
    }

    @Test
    void shouldCreateIndex() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class)))
                .thenReturn(mock(CreateIndexResponse.class));

        provider.createIndex("cmp-complaint-v1");

        verify(indicesClient).create(any(CreateIndexRequest.class));
    }

    @Test
    void shouldDeleteIndex() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.delete(any(DeleteIndexRequest.class)))
                .thenReturn(mock(DeleteIndexResponse.class));

        provider.deleteIndex("cmp-complaint-v1");

        verify(indicesClient).delete(any(DeleteIndexRequest.class));
    }

    @Test
    void shouldIndexDocument() throws Exception {
        when(openSearchClient.index(any(Function.class)))
                .thenReturn(mock(org.opensearch.client.opensearch.core.IndexResponse.class));

        provider.indexDocument("cmp-complaint", "doc-1", "{\"title\":\"test\"}");

        verify(openSearchClient).index(any(Function.class));
    }

    @Test
    void shouldReturnIndexExists() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(new BooleanResponse(true));

        assertThat(provider.indexExists("cmp-complaint-v1")).isTrue();
    }

    @Test
    void shouldWrapEngineFailureAsSearchEngineException() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> provider.createIndex("cmp-complaint-v1"))
                .isInstanceOf(SearchEngineException.class)
                .hasMessageContaining("Failed to create OpenSearch index");
    }

    @Test
    void shouldSwitchAlias() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.updateAliases(any(UpdateAliasesRequest.class)))
                .thenReturn(mock(UpdateAliasesResponse.class));

        provider.switchAlias("cmp-complaint", "cmp-complaint-v2", "cmp-complaint-v1");

        verify(indicesClient).updateAliases(any(UpdateAliasesRequest.class));
    }

    @Test
    void shouldBulkIndexInBatches() throws Exception {
        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.errors()).thenReturn(false);
        when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        BulkOperationResult result = provider.bulkIndex(
                "cmp-complaint-v1",
                List.of(
                        new EngineDocumentRequest("1", "{\"id\":\"1\"}"),
                        new EngineDocumentRequest("2", "{\"id\":\"2\"}"),
                        new EngineDocumentRequest("3", "{\"id\":\"3\"}")));

        assertThat(result.successCount()).isEqualTo(3);
        assertThat(result.failureCount()).isZero();
    }

    @Test
    void shouldBulkDeleteDocuments() throws Exception {
        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.errors()).thenReturn(false);
        when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        BulkOperationResult result = provider.bulkDelete("cmp-complaint-v1", List.of("1", "2"));

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failureCount()).isZero();
    }

    @Test
    void shouldReturnUpWhenClusterIsGreen() throws Exception {
        HealthResponse healthResponse = mock(HealthResponse.class);
        when(healthResponse.status()).thenReturn(HealthStatus.Green);
        when(openSearchClient.cluster()).thenReturn(clusterClient);
        when(clusterClient.health()).thenReturn(healthResponse);

        assertThat(provider.health()).isEqualTo(SearchEngineHealthStatus.UP);
    }

    @Test
    void shouldReturnDegradedWhenClusterIsYellow() throws Exception {
        HealthResponse healthResponse = mock(HealthResponse.class);
        when(healthResponse.status()).thenReturn(HealthStatus.Yellow);
        when(openSearchClient.cluster()).thenReturn(clusterClient);
        when(clusterClient.health()).thenReturn(healthResponse);

        assertThat(provider.health()).isEqualTo(SearchEngineHealthStatus.DEGRADED);
    }

    @Test
    void shouldReturnDownWhenClusterHealthFails() throws Exception {
        when(openSearchClient.cluster()).thenReturn(clusterClient);
        when(clusterClient.health()).thenThrow(new RuntimeException("unreachable"));

        assertThat(provider.health()).isEqualTo(SearchEngineHealthStatus.DOWN);
    }

    @Test
    void shouldCountBulkFailures() throws Exception {
        BulkResponse bulkResponse = mock(BulkResponse.class);
        BulkResponseItem failedItem = mock(BulkResponseItem.class);
        when(failedItem.error()).thenReturn(mock(org.opensearch.client.opensearch._types.ErrorCause.class));
        when(bulkResponse.errors()).thenReturn(true);
        when(bulkResponse.items()).thenReturn(List.of(failedItem, mock(BulkResponseItem.class)));
        when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        BulkOperationResult result = provider.bulkIndex(
                "cmp-complaint-v1",
                List.of(new EngineDocumentRequest("1", "{\"id\":\"1\"}")));

        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
    }
}
