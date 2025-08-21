package com.practice.mongoapi;

import com.practice.mongoapi.dto.BookDto;
import com.practice.mongoapi.model.Book;
import com.practice.mongoapi.repo.BookRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookApiIT {


    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @LocalServerPort
    int port;

    @Autowired
    BookRepository repo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        repo.deleteAll();
        ensureIndex(); // Ensure index is created before each test
    }

    private void ensureIndex() {
        IndexOperations indexOps = mongoTemplate.indexOps(Book.class);
        indexOps.dropAllIndexes(); // Clean up existing indexes

        // Create the compound unique index using Index class
        Index index = new Index()
                .on("title", Sort.Direction.ASC)
                .on("author", Sort.Direction.ASC)
                .named("unique_title_author")
                .unique();

        indexOps.ensureIndex(index);
    }

    @Test
    void create_get_update_delete_flow() {
        String id =
        given()
            .contentType("application/json")
            .body(new BookDto("Clean Code", "Robert Martin", 39.99, java.util.List.of("craft","clean")))
        .when()
            .post("/api/books")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("Clean Code"))
            .extract().path("id");

        given().when().get("/api/books/{id}", id)
            .then().statusCode(200)
            .body("author", equalTo("Robert Martin"));

        given()
            .contentType("application/json")
            .body(new BookDto("Clean Code", "Robert Martin", 29.99, java.util.List.of("craft")))
        .when()
            .put("/api/books/{id}", id)
        .then()
            .statusCode(200)
            .body("price", equalTo(29.99f));

        given().when().get("/api/books?page=0&size=10")
            .then().statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(1));

        given().when().delete("/api/books/{id}", id)
            .then().statusCode(204);

        given().when().get("/api/books/{id}", id)
            .then().statusCode(404);
    }

    @Test
    void validation_and_conflict() {
        given().contentType("application/json")
            .body(new BookDto("", "X", 10.0, java.util.List.of()))
        .when()
            .post("/api/books")
        .then()
            .statusCode(400);

        String id = given().contentType("application/json")
            .body(new BookDto("DDD", "Evans", 42.0, java.util.List.of()))
        .when()
            .post("/api/books")
        .then()
            .statusCode(201).extract().path("id");

        given().contentType("application/json")
            .body(new BookDto("DDD", "Evans", 42.0, java.util.List.of()))
        .when()
            .post("/api/books")
        .then()
            .statusCode(409);

        given().when().delete("/api/books/{id}", id).then().statusCode(204);
    }
}
