package com.practice.mongoapi.repo;

import com.practice.mongoapi.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findByAuthorIgnoreCase(String author);
    List<Book> findByTitleContainingIgnoreCase(String q);
}
