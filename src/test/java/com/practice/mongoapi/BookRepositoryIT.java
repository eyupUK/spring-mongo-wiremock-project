package com.practice.mongoapi;

import com.practice.mongoapi.model.Book;
import com.practice.mongoapi.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Testcontainers
class BookRepositoryIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0"); // Wait for MongoDB to be ready


    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    BookRepository repo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void clean() {
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
    void save_and_query() {
        repo.save(new Book(null, "Refactoring", "Martin Fowler", 45.0, List.of("refactor")));
        repo.save(new Book(null, "Patterns", "GoF", 50.0, List.of("design")));

        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByAuthorIgnoreCase("martin fowler").size());
        assertEquals(1, repo.findByTitleContainingIgnoreCase("pat").size());
    }

    @Test
    void unique_title_author() {
        repo.save(new Book(null, "Same", "Author", 10.0, List.of()));
        assertThrows(DuplicateKeyException.class, () ->
            repo.save(new Book(null, "Same", "Author", 12.0, List.of()))
        );
    }
}
