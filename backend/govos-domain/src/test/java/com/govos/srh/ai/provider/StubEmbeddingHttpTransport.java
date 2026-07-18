package com.govos.srh.ai.provider;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StubEmbeddingHttpTransport implements EmbeddingHttpTransport {

    private final List<Function<HttpRequest, HttpResponse<String>>> responders = new ArrayList<>();
    private int callCount;

    public void enqueue(HttpResponse<String> response) {
        responders.add(request -> response);
    }

    public void enqueue(Function<HttpRequest, HttpResponse<String>> responder) {
        responders.add(responder);
    }

    @Override
    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        callCount++;
        if (responders.isEmpty()) {
            throw new IOException("No stub response configured");
        }
        Function<HttpRequest, HttpResponse<String>> responder = responders.removeFirst();
        return responder.apply(request);
    }

    public int getCallCount() {
        return callCount;
    }
}
