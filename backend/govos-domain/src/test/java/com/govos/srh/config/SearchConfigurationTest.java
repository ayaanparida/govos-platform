package com.govos.srh.config;

import com.govos.srh.engine.OpenSearchEngineProvider;
import com.govos.srh.engine.SearchEngineProvider;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SearchConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SearchConfiguration.class, JacksonAutoConfiguration.class)
            .withPropertyValues(
                    "govos.search.host=localhost",
                    "govos.search.port=9200",
                    "govos.search.username=admin",
                    "govos.search.password=secret",
                    "govos.search.ssl=false");

    @Test
    void shouldCreateOpenSearchBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SearchProperties.class);
            assertThat(context).hasSingleBean(RestClient.class);
            assertThat(context).hasSingleBean(OpenSearchTransport.class);
            assertThat(context).hasSingleBean(OpenSearchClient.class);
            assertThat(context.getBean(SearchProperties.class).getHost()).isEqualTo("localhost");
            assertThat(context.getBean(SearchProperties.class).getPort()).isEqualTo(9200);
        });
    }

    @Test
    void shouldBindSearchPropertiesFromConfiguration() {
        contextRunner.run(context -> {
            SearchProperties properties = context.getBean(SearchProperties.class);
            assertThat(properties.getUsername()).isEqualTo("admin");
            assertThat(properties.getPassword()).isEqualTo("secret");
            assertThat(properties.isSsl()).isFalse();
            assertThat(properties.getBulkBatchSize()).isEqualTo(500);
        });
    }
}
