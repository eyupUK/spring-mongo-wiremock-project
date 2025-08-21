package com.practice.mongoapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class BookDto {
    @NotBlank
    private String title;
    @NotBlank
    private String author;
    @Positive
    private double price;
    private List<String> tags;

    public BookDto() {}

    public BookDto(String title, String author, double price, List<String> tags) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.tags = tags;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
