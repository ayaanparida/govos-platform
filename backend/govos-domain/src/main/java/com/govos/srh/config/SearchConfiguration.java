package com.govos.srh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.production.SearchPoolProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.client.RestClientBuilder;import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class SearchConfiguration {

    @Bean(destroyMethod = "close")
    public RestClient openSearchRestClient(SearchProperties searchProperties) {
        String scheme = searchProperties.isSsl() ? "https" : "http";
        HttpHost host = new HttpHost(searchProperties.getHost(), searchProperties.getPort(), scheme);
        SearchPoolProperties pool = searchProperties.getPool();

        RestClientBuilder builder = RestClient.builder(host)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(pool.getConnectionTimeoutMs())
                        .setSocketTimeout(pool.getSocketTimeoutMs()))
                .setHttpClientConfigCallback(httpClientBuilder -> configureHttpClient(
                        httpClientBuilder, searchProperties, pool));

        return builder.build();
    }

    private HttpAsyncClientBuilder configureHttpClient(
            HttpAsyncClientBuilder httpClientBuilder,
            SearchProperties searchProperties,
            SearchPoolProperties pool) {
        httpClientBuilder.setMaxConnTotal(pool.getMaxConnections());
        httpClientBuilder.setMaxConnPerRoute(pool.getMaxConnectionsPerRoute());

        if (searchProperties.getUsername() != null && !searchProperties.getUsername().isBlank()) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            searchProperties.getUsername(),
                            searchProperties.getPassword() != null ? searchProperties.getPassword() : ""));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        return httpClientBuilder;
    }

    @Bean(destroyMethod = "close")
    public OpenSearchTransport openSearchTransport(RestClient openSearchRestClient, ObjectMapper objectMapper) {
        return new RestClientTransport(openSearchRestClient, new JacksonJsonpMapper(objectMapper));
    }

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchTransport openSearchTransport) {
        return new OpenSearchClient(openSearchTransport);
    }
}
