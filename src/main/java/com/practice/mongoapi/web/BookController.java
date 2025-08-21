package com.practice.mongoapi.web;

import com.practice.mongoapi.dto.BookDto;
import com.practice.mongoapi.dto.PriceQuote;
import com.practice.mongoapi.model.Book;
import com.practice.mongoapi.service.BookService;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid BookDto dto) {
        try {
            Book saved = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate book (title+author must be unique).");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        return service.get(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"));
    }

    @GetMapping
    public Page<Book> list(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return service.list(page, size);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody @Valid BookDto dto) {
        try {
            Book updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate book (title+author must be unique).");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/by-author")
    public List<Book> byAuthor(@RequestParam String author) { return service.findByAuthor(author); }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String q) { return service.search(q); }

    @GetMapping("/{id}/price-with-discount")
    public ResponseEntity<?> priceWithDiscount(@PathVariable String id) {
        try {
            PriceQuote quote = service.quotePrice(id);
            return ResponseEntity.ok(quote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Pricing service error");
        }
    }
}
