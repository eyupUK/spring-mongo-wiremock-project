# Spring Boot + MongoDB + WireMock

A small CRUD API to practice **API tests (Rest Assured)**, **DB tests (Testcontainers MongoDB)**, and **service virtualization (WireMock)**.

## Run API locally
```bash
docker compose up -d
export MONGODB_URI="mongodb://localhost:27017/practice"
mvn spring-boot:run
```

## Endpoints
- `POST   /api/books`
- `GET    /api/books/{id}`
- `GET    /api/books?page=0&size=10`
- `PUT    /api/books/{id}`
- `DELETE /api/books/{id}`
- `GET    /api/books/by-author?author=...`
- `GET    /api/books/search?q=...`
- `GET    /api/books/{id}/price-with-discount` ← calls external Pricing service (stubbed by WireMock in tests)

## Tests
- **API E2E** with Rest Assured + Testcontainers (`BookApiIT`)
- **Repository** slice with Testcontainers (`BookRepositoryIT`)
- **WireMock** test stubbing external pricing (`BookPricingWireMockIT`)

Run all tests:
```bash
mvn clean test
```
Run only WireMock test:
```bash
mvn -Dtest=BookPricingWireMockIT test
```
Requires Docker for Testcontainers.
