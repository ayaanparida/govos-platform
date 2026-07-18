package com.govos.srh.ai.provider;

import java.net.http.HttpResponse;
import org.mockito.Mockito;

final class HttpResponseStub {

    private HttpResponseStub() {
    }

    static HttpResponse<String> of(int statusCode, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(statusCode);
        Mockito.when(response.body()).thenReturn(body);
        return response;
    }
}
