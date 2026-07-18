package com.govos.srh.ai.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class DefaultEmbeddingHttpTransport implements EmbeddingHttpTransport {

    private final HttpClient httpClient;

    public DefaultEmbeddingHttpTransport() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    DefaultEmbeddingHttpTransport(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
