# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start infrastructure (PostgreSQL + pgAdmin + MinIO)
docker-compose up -d

# Run the application
./mvnw spring-boot:run -pl app

# Build all modules (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run tests in a specific module
./mvnw test -pl app

# Run a single test class
./mvnw test -pl app -Dtest=SnapshotShowcaseResolverTest

# Run SpotBugs analysis
./mvnw spotbugs:check
```

Required env vars (see `docker-compose.yml`): `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`.

Swagger UI is at `/swagger-ui.html`. Actuator at `/actuator/health`.

## Architecture

Multi-module Maven monorepo. Each business module is independent; the `app` module aggregates them all into a single deployable Spring Boot JAR.

**Modules**: `shared` · `attribute` · `product` · `inventory` · `pricing` · `storage` · `catalog` · `order` · `app`

### Within each module: Hexagonal layers

```
domain/
  entity/         — pure domain objects (no Spring, no persistence)
  port/           — repository interfaces owned by the domain
  exception/      — domain rule violations
application/
  usecase/        — one class per command/write operation
  query/          — read-only services and query repositories
  command/        — input DTOs for use cases
  port/out/       — ports toward external systems (resolved by infrastructure adapters)
infrastructure/
  persistence/    — JDBI repository implementations
  adapter/        — adapters to other modules (e.g. inventory → pricing)
presentation/
  controller/     — Spring MVC controllers
  dto/            — request/response DTOs
```

### Domain entity conventions

Domain entities never use JPA annotations. They expose two static factory methods:
- `Entity.create(...)` — constructs a new entity with initial state
- `Entity.restore(...)` — reconstructs from persisted data (skips invariant checks meant for creation)

State transitions are methods on the entity itself (e.g., `order.confirm()`, `order.expire()`) and throw `IllegalStateException` for invalid transitions.

### Persistence: JDBI (not JPA)

All database access uses JDBI. The `Jdbi` bean is configured in `shared` (`JdbiConfig.java`) and injected into repository classes across all modules. Migrations are Flyway SQL files — **all** of them live in `shared/src/main/resources/db/migration/`, regardless of which module a table belongs to.

### Inter-module communication

Modules communicate via **Spring Application Events** (not direct method calls), preventing circular Maven dependencies:

- `ProductPublishedEvent` / `ProductUnpublishedEvent` (product → catalog) — triggers async catalog sync after commit via `@TransactionalEventListener(phase = AFTER_COMMIT) @Async`
- `InventoryReservationChangedEvent` (inventory → catalog) — updates available stock display

Events shared between modules are declared in `shared/src/main/java/br/com/erp/api/shared/event/`.

### CQRS split

Modules with complex read requirements separate reads from writes:
- Write: `*UseCase` classes (e.g., `CreateOrderUseCase`, `ConfirmOrderUseCase`)
- Read: `*QueryService` + `*QueryRepository` (e.g., `OrderAdminQueryService`, `CatalogQueryService`)

This is most visible in `catalog` (read-only public store API) and `order` (admin listing with filters/pagination).

### Error handling

All exceptions bubble to `GlobalExceptionHandler` in the `app` module, which returns `application/problem+json` (RFC 7807 `ProblemDetail`). The hierarchy:
- `DomainException` (shared) → 422 Unprocessable Entity
- `EntityNotFoundException` (shared) → 404 Not Found
- Module-specific exceptions extend these or are mapped directly in the handler

### Key integrations

- **MinIO** (`storage` module): images are uploaded client-side using pre-signed URLs; the backend only generates and confirms them
- **Next.js revalidation** (`catalog` module): after catalog sync, calls Next.js revalidate endpoint via `NextJsRevalidationAdapter`
- **WhatsApp** (`order` module): order confirmation generates a pre-filled WhatsApp URL for the seller
- **OpenAI** (`product` module): AI-generated product descriptions via `AiTextGeneratorPort`

### Testing

Unit tests use plain JUnit 5 with no Spring context. Integration tests use `@SpringBootTest` + Testcontainers (PostgreSQL). Test `application.yml` switches to H2 in PostgreSQL mode with Flyway disabled.