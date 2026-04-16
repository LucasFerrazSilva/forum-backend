# AGENTS.md — Forum Back-end

## Project Overview
Spring Boot 4 REST API (Java 21) for a forum platform. Uses PostgreSQL with Flyway migrations, session-based auth (HTTP-only cookie), and bcrypt+pepper password hashing. No Spring Security filter chain — security is custom-rolled via `CustomPasswordEncoder` and session tokens stored in `TB_SESSIONS`.

## Package Structure
```
com.ferraz.forumbackend
├── infra/
│   ├── exception/   # Global error handling (ExceptionsHandler, BaseException hierarchy)
│   └── security/    # CustomPasswordEncoder (BCrypt + pepper), CorsConfiguration
├── user/            # UserEntity, UserController, UserService, UserRepository, UserMapper
├── session/         # SessionEntity, SessionController, SessionService, SessionRepository
└── status/          # Health/status endpoint
```
Each domain is self-contained (entity, controller, service, repository, DTOs, exceptions in one package).

## Key Patterns

### Error Handling
Throw subclasses of `BaseException` (e.g., `NotFoundException`, `DatabaseException`, `ValidationException`).  
`ExceptionsHandler` (`@RestControllerAdvice`) catches all and returns `ErrorResponse`.  
Never throw raw `RuntimeException` — always use a typed `BaseException` subclass.

### DTOs & Mapping
Use static methods in `UserMapper` (e.g., `UserMapper.toDTO(entity)`). No MapStruct. DTOs live in `<domain>/dto/`.

### Authentication
Login → `POST /api/v1/sessions` returns `session_id` as an HTTP-only cookie. Token stored in `TB_SESSIONS` with expiry.  
Password: `CustomPasswordEncoder` applies a pepper suffix before BCrypt hashing. Requires `security.pepper` env var (no default).

### Database
Hibernate `ddl-auto: validate` — schema is managed **only** via Flyway migrations in `src/main/resources/db/migration/`.  
Add new migrations as `V{n}__Description.sql`. Never alter existing migration files.

### Required Environment Variables
| Variable | Default | Notes |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/forum` | |
| `DATABASE_USER` | `forum_user` | |
| `DATABASE_PASSWORD` | `forum_password` | |
| `PASSWORD_PEPPER` | *(none)* | **Required** — no default |
| `PASSWORD_ROUNDS` | `14` | BCrypt cost factor |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,...` | |
| `COOKIE_SECURE` | `false` | Set `true` in production |

## Developer Workflows

### Run locally
Docker Compose is auto-started by Spring Boot DevTools when running the app (`compose.yaml` spins up Postgres).
```
./mvnw spring-boot:run
```

### Build
```
./mvnw package
```

### Tests
- **Unit tests** (`src/test/.../unit/`): run with `./mvnw test` (Surefire, no DB needed)
- **Integration tests** (`src/test/.../integration/`): run with `./mvnw verify` (Failsafe, uses Testcontainers PostgreSQL)
- Integration tests extend `AbstractIntegrationTest`, which resets the DB via `flyway.clean()` + `flyway.migrate()` before each test class.
- Use `compose-all.yaml` for full-stack local runs; `compose.yaml` is for dev only.

### Coverage
```
./mvnw verify   # generates target/site/jacoco/jacoco.xml (used by SonarCloud)
```

## API Endpoints
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users` | Register user |
| `GET` | `/api/v1/users/{username}` | Get user |
| `PATCH` | `/api/v1/users/{username}` | Update user |
| `POST` | `/api/v1/sessions` | Login (sets `session_id` cookie) |
| `GET` | `/actuator/...` | Spring Actuator (status/info) |

## Key Files
- `src/main/resources/application.yml` — all config with env var defaults
- `src/main/resources/db/migration/` — Flyway SQL migrations (source of truth for schema)
- `src/test/java/.../integration/AbstractIntegrationTest.java` — base class for all integration tests
- `src/main/java/.../infra/exception/ExceptionsHandler.java` — global error handler
- `src/main/java/.../infra/security/CustomPasswordEncoder.java` — pepper+bcrypt logic

