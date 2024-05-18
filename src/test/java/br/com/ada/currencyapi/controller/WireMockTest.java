package br.com.ada.currencyapi.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import wiremock.org.apache.hc.client5.http.classic.methods.HttpGet;
import wiremock.org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import wiremock.org.apache.hc.client5.http.impl.classic.HttpClients;
import wiremock.org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WireMockTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        configureFor("localhost", 8081);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testOffAirApi() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        stubFor(get(urlEqualTo("/currency/api-convert?from=USD&to=BRL&amount=2"))
                .willReturn(aResponse().withStatus(503)));

        HttpGet request = new HttpGet("http://localhost:8081/currency/api-convert?from=USD&to=BRL&amount=2");
        HttpResponse httpResponse = httpClient.execute(request);

        assertEquals(503, httpResponse.getCode());
    }

    @Test
    void testSuccessfulApiCall() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        stubFor(get(urlEqualTo("/currency/api-convert?from=USD&to=BRL&amount=2"))
                .willReturn(aResponse().withStatus(200)));

        HttpGet request = new HttpGet("http://localhost:8081/currency/api-convert?from=USD&to=BRL&amount=2");
        HttpResponse httpResponse = httpClient.execute(request);

        assertEquals(200, httpResponse.getCode());
    }
}