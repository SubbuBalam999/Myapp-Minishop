package com.subbu.minishop.apigateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTest {

    private static HttpServer userService;
    private static HttpServer productService;

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startDownstreamServices() throws IOException {
        userService = startServer("/api/users/1", """
                {"id":1,"name":"Ada Lovelace","email":"ada@example.com"}
                """);
        productService = startServer("/api/products/1", """
                {"id":1,"name":"Mechanical Keyboard","price":89.99}
                """);
    }

    @AfterAll
    static void stopDownstreamServices() {
        userService.stop(0);
        productService.stop(0);
    }

    @DynamicPropertySource
    static void configureDownstreamServices(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:" + userService.getAddress().getPort());
        registry.add("PRODUCT_SERVICE_URL", () -> "http://localhost:" + productService.getAddress().getPort());
    }

    @Test
    void routesUserRequests() {
        client().get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Ada Lovelace");
    }

    @Test
    void routesProductRequests() {
        client().get()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Mechanical Keyboard");
    }

    @Test
    void returnsGatewayHealth() {
        client().get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void exposesActuatorHealth() {
        client().get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void exposesPrometheusMetrics() {
        client().get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/plain");
    }

    private WebTestClient client() {
        return webTestClient.mutate()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private static HttpServer startServer(String path, String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(path, exchange -> writeJsonResponse(exchange, responseBody));
        server.start();
        return server;
    }

    private static void writeJsonResponse(HttpExchange exchange, String responseBody) throws IOException {
        byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
