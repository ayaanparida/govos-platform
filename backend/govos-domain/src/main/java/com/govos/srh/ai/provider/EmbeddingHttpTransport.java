package com.govos.srh.ai.provider;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface EmbeddingHttpTransport {

    HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException;
}
