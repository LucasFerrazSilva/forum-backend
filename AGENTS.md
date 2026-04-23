# AGENTS.md — Forum Back-end

## Project Overview
Spring Boot 4 REST API (Java 21) for a forum platform. Uses PostgreSQL with Flyway migrations, session-based auth (HTTP-only cookie), and bcrypt+pepper password hashing. No Spring Security filter chain — security is custom-rolled via `CustomPasswordEncoder` and session tokens stored in `TB_SESSIONS`.

## Package Structure
```
com.ferraz.forumbackend
├── infra/
│   ├── exception/        # Global error handling (ExceptionsHandler, BaseException hierarchy)
│   ├── security/         # CustomPasswordEncoder (BCrypt + pepper), CorsConfiguration
│   ├── CookieService     # Session cookie creation/reading (HTTP-only, configurable secure flag)
│   └── EmailService      # Async email dispatch via JavaMailSender (@Async)
├── activationtoken/      # ActivationTokenEntity, Controller, Service, Repository + InvalidActivationTokenException
├── user/
│   ├── validator/        # InsertUserValidator, UpdateUserValidator interfaces + implementations
│   └── ...               # UserEntity, UserController, UserService, UserRepository, UserMapper
├── session/              # SessionEntity, SessionController, SessionService, SessionRepository
└── status/               # StatusController, StatusService, StatusDAO, entity/ (StatusDTO, DatabaseInfo)
```
Each domain is self-contained (entity, controller, service, repository, DTOs, exceptions in one package).

## Key Patterns

### Error Handling
Throw subclasses of `BaseException` (e.g., `NotFoundException`, `DatabaseException`, `ValidationException`).  
`ExceptionsHandler` (`@RestControllerAdvice`) catches all and returns `ErrorResponse`.  
Never throw raw `RuntimeException` — always use a typed `BaseException` subclass.

### DTOs & Mapping
Use static methods in `UserMapper` (e.g., `UserMapper.toDTO(entity)`). No MapStruct. DTOs live in `<domain>/dto/`.

### Validation
`UserService` injects `List<InsertUserValidator>` and `List<UpdateUserValidator>` — add a validator by implementing the interface and annotating it `@Component`. Validators throw `BaseException` subclasses (never return booleans). Examples: `UniqueEmailValidator`, `UniqueUsernameValidator`, `UsernameExistsValidator` in `user/validator/`.

### Authentication
Login → `POST /api/v1/sessions` returns `session_id` as an HTTP-only cookie. Token stored in `TB_SESSIONS` with expiry.  
Cookie lifecycle is managed by `CookieService` (create, expire, extract from request). Cookie name is configured via `server.cookie.name` (`session_id`).  
Password: `CustomPasswordEncoder` applies a pepper suffix before BCrypt hashing. Requires `security.pepper` env var (defaults to literal `"PASSWORD_PEPPER"` in dev — **set a real value in production**).

### User Features
`UserEntity` has a `String[] features` column. Registration sets `"read:activation_token"`. Account activation via `GET /api/v1/activation-token/activate/{id}` sets `"usuario-ativado"`. Future permission flags follow this same pattern.

### Account Activation Flow
`POST /api/v1/users` → creates user → creates `ActivationTokenEntity` in `TB_ACTIVATION_TOKENS` → sends activation email (`EmailService`, async) with link to `GET /api/v1/activation-token/activate/{id}`. Activating marks `usedAt` on the token and adds `"usuario-ativado"` feature to the user. Tokens are single-use (second activation attempt throws `InvalidActivationTokenException`).

### Database
Hibernate `ddl-auto: validate` — schema is managed **only** via Flyway migrations in `src/main/resources/db/migration/`.  
Add new migrations as `V{n}__Description.sql`. Never alter existing migration files.

### Required Environment Variables
| Variable | Default | Notes |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/forum` | |
| `DATABASE_USER` | `forum_user` | |
| `DATABASE_PASSWORD` | `forum_password` | |
| `PASSWORD_PEPPER` | `"PASSWORD_PEPPER"` | **Set a real value in production** |
| `PASSWORD_ROUNDS` | `14` | BCrypt cost factor |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,...` | |
| `COOKIE_SECURE` | `false` | Set `true` in production |
| `ACTIVATION_TOKEN_BASE_URL` | `http://localhost:8080` | Base URL prepended to activation links in emails |
| `ACTIVATION_TOKEN_EXPIRATION_DAYS` | `7` | Days until an activation token expires |
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port |
| `MAIL_USERNAME` | `contato@forum.com` | From address for outgoing emails |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
| `MAIL_AUTH` | `false` | Enable SMTP auth |
| `MAIL_TLS` | `false` | Enable STARTTLS |
| `SHOW_SQL` | `true` | Log Hibernate SQL |

## Versioning

**Always bump `<version>` in `pom.xml` with every change**, following [Semantic Versioning](https://semver.org/) (`MAJOR.MINOR.PATCH`):

| Change type | What to bump | Example |
|---|---|---|
| Breaking change / incompatible API | `MAJOR` | `1.1.0` → `2.0.0` |
| New feature, backwards-compatible | `MINOR` | `1.1.0` → `1.2.0` |
| Bug fix, refactor, config/infra change | `PATCH` | `1.1.0` → `1.1.1` |

The version in `pom.xml` is used directly as the Docker image tag in CI (`pull_request_workflow.yaml`), so it must be updated before merging to `main`.

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
- **One test class per endpoint per domain** — each class covers all scenarios for a single HTTP method + path combination (e.g., `CreateUserIntegrationTest`, `GetUserByUsernameIntegrationTest`).
- Use `compose-all.yaml` for full-stack local runs; `compose.yaml` is for dev only.

> **⚠️ Mandatory for every new feature:** tests are part of the feature — not optional.  
> Every new endpoint or behaviour change **must** include corresponding unit and/or integration tests.  
> After writing the tests, **run `./mvnw verify`** and confirm the full suite is green before considering the task done.  
> Never deliver a feature without running the tests and confirming they pass.

### Coverage
```
./mvnw verify   # generates target/site/jacoco/jacoco.xml (used by SonarCloud)
```

## Integration Test Structure
Each domain has one test class per endpoint, all inside `src/test/java/.../integration/<domain>/`:

| Class | Method | Path |
|---|---|---|
| `CreateUserIntegrationTest` | `POST` | `/api/v1/users` |
| `GetUserByUsernameIntegrationTest` | `GET` | `/api/v1/users/{username}` |
| `GetCurrentUserIntegrationTest` | `GET` | `/api/v1/users` (requires session cookie) |
| `UpdateUserIntegrationTest` | `PATCH` | `/api/v1/users/{username}` |
| `RegisterUserFlowIntegrationTest` | multi | End-to-end: register → activation email → activate → login |
| `CreateSessionIntegrationTest` | `POST` | `/api/v1/sessions` |
| `DeleteSessionIntegrationTest` | `DELETE` | `/api/v1/sessions` |
| `ActivateAccountIntegrationTest` | `GET` | `/api/v1/activation-token/activate/{id}` |

When adding a new endpoint, create a new `<Action><Domain>IntegrationTest.java` class in the matching domain package, extending `AbstractIntegrationTest`.

### Test Infrastructure
- **`TestcontainersConfig`** — spins up `postgres:17` and a `GreenMail` SMTP stub. Both are shared across all test classes (static, `withReuse(true)`).
- **`AbstractIntegrationTest`** exposes `greenMail` (verify emails), `userFixture`, `sessionFixture`, `cookieName`. `@BeforeAll` resets DB; `@BeforeEach` purges GreenMail inbox.
- **`MvcRequestBuilder`** — fluent builder for test HTTP calls. Use `GET()/POST()/PATCH()/DELETE()` from `AbstractIntegrationTest`, chain `.withRequestBody()`, `.shouldAuthenticateWithNewUser()`, `.withSessionCookie()`, then `.send()`.
- **`UserFixture` / `SessionFixture` / `ActivationTokenFixture`** — create test data via inner builder classes (e.g., `userFixture.user(b -> b.username("alice"))`, `activationTokenFixture.token(user, b -> b.expiresAt(LocalDateTime.now().minusDays(1)))`). Use these instead of calling repositories directly.

## API Endpoints
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users` | Register user (also creates activation token + sends email) |
| `GET` | `/api/v1/users/{username}` | Get user |
| `PATCH` | `/api/v1/users/{username}` | Update user |
| `GET` | `/api/v1/users` | Get current user via session cookie |
| `POST` | `/api/v1/sessions` | Login (sets `session_id` cookie) |
| `DELETE` | `/api/v1/sessions` | Logout |
| `GET` | `/api/v1/activation-token/activate/{id}` | Activate account (sets `"usuario-ativado"` feature) |
| `GET` | `/actuator/...` | Spring Actuator (status/info) |

## Key Files
- `src/main/resources/application.yml` — all config with env var defaults
- `src/main/resources/db/migration/` — Flyway SQL migrations (source of truth for schema)
- `src/test/java/.../integration/AbstractIntegrationTest.java` — base class for all integration tests
- `src/test/java/.../integration/util/MvcRequestBuilder.java` — fluent HTTP test builder
- `src/test/java/.../integration/fixture/UserFixture.java` — test user data factory
- `src/test/java/.../integration/fixture/SessionFixture.java` — test session/cookie factory
- `src/test/java/.../integration/fixture/ActivationTokenFixture.java` — test activation token factory
- `src/test/java/.../integration/util/TestcontainersConfig.java` — Postgres + GreenMail container setup
- `src/main/java/.../infra/exception/ExceptionsHandler.java` — global error handler
- `src/main/java/.../infra/security/CustomPasswordEncoder.java` — pepper+bcrypt logic
- `src/main/java/.../infra/CookieService.java` — session cookie management
- `src/main/java/.../infra/EmailService.java` — async email dispatch

