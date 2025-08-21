package com.practice.mongoapi;

import com.practice.mongoapi.dto.BookDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookPricingWireMockIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    static WireMockServer wm = new WireMockServer(options().dynamicPort());
    static { wm.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        r.add("spring.data.mongodb.database", () -> "wiremock_it"); // 👈 important
        r.add("pricing.base-url", () -> wm.baseUrl());
    }

    @LocalServerPort
    int port;

    private String api(String path) { return "http://localhost:" + port + path; }

    @AfterAll
    static void shutdown() { wm.stop(); }

    @Test
    void price_with_discount_positive() {
        // 1) Create the book
        String id =
                given().log().all()
                        .contentType("application/json")
                        .body(new BookDto("Clean Code", "Robert Martin", 39.99, java.util.List.of("craft")))
                        .when()
                        .post(api("/api/books"))
                        .then().log().ifValidationFails()
                        .statusCode(201)
                        .extract().path("id");

        // 2) Sanity: the book exists in the same DB
        given().log().all()
                .get(api("/api/books/{id}"), id)
                .then().log().ifValidationFails()
                .statusCode(200);

        // 3) Stub pricing on WireMock (be lenient on double formatting)
        wm.stubFor(get(urlPathEqualTo("/discount"))
                .withQueryParam("title", equalTo("Clean Code"))
                .withQueryParam("author", equalTo("Robert Martin"))
                .withQueryParam("price", matching("39\\.99(?:0+)?"))
                .willReturn(
                        okJson("{\"discountPct\": 0.20, \"finalPrice\": 31.99}")
                                .withHeader("Content-Type", "application/json")
                ));

        // 4) Call YOUR endpoint (not WireMock)
        given().log().all()
                .pathParam("id", id)
                .when()
                .get(api("/api/books/{id}/price-with-discount"))
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("bookId", (ResponseAwareMatcher<Response>) equalTo(id))
                .body("discountPct", closeTo(0.20f, 0.0001f))
                .body("finalPrice", closeTo(31.99f, 0.01f));

        // 5) Optional: verify WireMock was hit
        wm.verify(getRequestedFor(urlPathEqualTo("/discount"))
                .withQueryParam("title", equalTo("Clean Code")));
    }


    @Test
    void price_with_discount_external_error_results_in_502() {
        String id = given().contentType("application/json")
                .body(new BookDto("DDD", "Evans", 42.00, java.util.List.of()))
                .post(api("/api/books")).then().statusCode(201)
                .extract().path("id");

        wm.stubFor(get(urlPathEqualTo("/discount"))
                .withQueryParam("title", equalTo("DDD"))
                .withQueryParam("author", equalTo("Evans"))
                .withQueryParam("price", equalTo("42.0"))
                .willReturn(serverError()));

        given().get(api("/api/books/{id}/price-with-discount"), id)
                .then().statusCode(502);
    }
}
