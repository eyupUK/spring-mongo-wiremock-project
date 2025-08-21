package com.practice.mongoapi.dto;

public class PriceQuote {
    private String bookId;
    private double originalPrice;
    private double discountPct;
    private double finalPrice;

    public PriceQuote() {}

    public PriceQuote(String bookId, double originalPrice, double discountPct, double finalPrice) {
        this.bookId = bookId;
        this.originalPrice = originalPrice;
        this.discountPct = discountPct;
        this.finalPrice = finalPrice;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
    public double getDiscountPct() { return discountPct; }
    public void setDiscountPct(double discountPct) { this.discountPct = discountPct; }
    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }
}
