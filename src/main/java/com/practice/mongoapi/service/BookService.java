package com.practice.mongoapi.service;

import com.practice.mongoapi.dto.BookDto;
import com.practice.mongoapi.dto.PriceQuote;
import com.practice.mongoapi.external.PricingClient;
import com.practice.mongoapi.model.Book;
import com.practice.mongoapi.repo.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository repo;
    private final PricingClient pricing;

    public BookService(BookRepository repo, PricingClient pricing) {
        this.repo = repo;
        this.pricing = pricing;
    }

    public Book create(BookDto dto) {
        Book b = new Book(null, dto.getTitle(), dto.getAuthor(), dto.getPrice(), dto.getTags());
        return repo.save(b);
    }

    public Optional<Book> get(String id) { return repo.findById(id); }

    public Page<Book> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size));
    }

    public Book update(String id, BookDto dto) {
        Book b = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
        b.setTitle(dto.getTitle());
        b.setAuthor(dto.getAuthor());
        b.setPrice(dto.getPrice());
        b.setTags(dto.getTags());
        return repo.save(b);
    }

    public void delete(String id) { repo.deleteById(id); }

    public List<Book> findByAuthor(String author) { return repo.findByAuthorIgnoreCase(author); }
    public List<Book> search(String q) { return repo.findByTitleContainingIgnoreCase(q); }

    public PriceQuote quotePrice(String id) {
        Book b = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
        double discount = pricing.fetchDiscountPct(b.getTitle(), b.getAuthor(), b.getPrice());
        double finalPrice = BigDecimal.valueOf(b.getPrice() * (1 - discount))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new PriceQuote(b.getId(), b.getPrice(), discount, finalPrice);
    }
}
