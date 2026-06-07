package com.subbu.minishop.apigateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    private static HttpServer cartService;
    private static HttpServer orderService;
    private static HttpServer inventoryService;
    private static HttpServer paymentService;

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
        cartService = startServer("/api/cart/1", """
                [{"id":1,"userId":1,"productId":1,"quantity":2}]
                """);
        orderService = startServer("/api/orders/1", """
                {"id":1,"userId":1,"totalAmount":1999.98,"status":"CREATED"}
                """);
        inventoryService = startServer("/api/inventory/1", """
                {"id":1,"productId":1,"availableQuantity":8,"reservedQuantity":2}
                """);
        paymentService = startServer("/api/payments/1", """
                {"id":1,"orderId":1,"userId":1,"amount":299.99,"status":"SUCCESS","paymentMethod":"CARD"}
                """);
    }

    @AfterAll
    static void stopDownstreamServices() {
        userService.stop(0);
        productService.stop(0);
        cartService.stop(0);
        orderService.stop(0);
        inventoryService.stop(0);
        paymentService.stop(0);
    }

    @DynamicPropertySource
    static void configureDownstreamServices(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:" + userService.getAddress().getPort());
        registry.add("PRODUCT_SERVICE_URL", () -> "http://localhost:" + productService.getAddress().getPort());
        registry.add("CART_SERVICE_URL", () -> "http://localhost:" + cartService.getAddress().getPort());
        registry.add("ORDER_SERVICE_URL", () -> "http://localhost:" + orderService.getAddress().getPort());
        registry.add(
                "INVENTORY_SERVICE_URL",
                () -> "http://localhost:" + inventoryService.getAddress().getPort()
        );
        registry.add(
                "PAYMENT_SERVICE_URL",
                () -> "http://localhost:" + paymentService.getAddress().getPort()
        );
    }

    @Test
    void routesUserRequests() {
        client().get()
                .uri("/api/users/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$.name").isEqualTo("Ada Lovelace");
    }

    @Test
    void routesProductRequests() {
        client().get()
                .uri("/api/products/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$.name").isEqualTo("Mechanical Keyboard");
    }

    @Test
    void routesCartRequests() {
        client().get()
                .uri("/api/cart/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$[0].quantity").isEqualTo(2);
    }

    @Test
    void routesOrderRequests() {
        client().get()
                .uri("/api/orders/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo("CREATED");
    }

    @Test
    void routesInventoryRequests() {
        client().get()
                .uri("/api/inventory/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$.availableQuantity").isEqualTo(8)
                .jsonPath("$.reservedQuantity").isEqualTo(2);
    }

    @Test
    void routesPaymentRequests() {
        client().get()
                .uri("/api/payments/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectBody()
                .jsonPath("$.status").isEqualTo("SUCCESS")
                .jsonPath("$.paymentMethod").isEqualTo("CARD");
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

    @Test
    void allowsFrontendCorsPreflightForUsers() {
        client().method(HttpMethod.OPTIONS)
                .uri("/api/users")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                .header(
                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                        "Content-Type, Authorization, Accept"
                )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST,PUT,DELETE,OPTIONS"
                )
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        "Content-Type, Authorization, Accept"
                )
                .expectHeader().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS
                );
    }

    @Test
    void allowsFrontendCorsPreflightForProducts() {
        client().method(HttpMethod.OPTIONS)
                .uri("/api/products")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                )
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST,PUT,DELETE,OPTIONS"
                )
                .expectHeader().valueEquals(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        "Authorization"
                );
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
