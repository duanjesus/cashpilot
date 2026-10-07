<div align="center">

# CashPilot — Backend

### Spring Boot backend for personal financial management: bank accounts, credit cards, income, expenses, transfers, goals and net-worth projection

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?logo=flyway)](https://flywaydb.org/)

</div>

> This is the backend half of the [CashPilot monorepo](../README.md). See the root README for the full-stack quick start (API + React frontend together).

---

## 🏗️ Architecture

The project follows a layered architecture with clear separation of concerns:

```
                                ┌───────────────────┐
                                │     Controller       │  → Handles HTTP requests, validates input (DTO)
                                └─────────┬───────────┘
                                          │
                                ┌─────────▼───────────┐
                                │       Service          │  → Business rules, ownership checks, transactions
                                └─────────┬───────────┘
                                          │
                         ┌────────────────┼────────────────┐
                         │                                 │
               ┌─────────▼──────────┐          ┌───────────▼───────────┐
               │      Mapper           │          │      Repository         │  → Data access (Spring Data JPA)
               │ (Entity → Response DTO) │          └───────────┬───────────┘
               └───────────────────────┘                      │
                                                      ┌────────▼────────┐
                                                      │   PostgreSQL       │
                                                      └───────────────────┘

        Business exceptions (ResourceNotFound, DuplicateResource, Business) are
        handled centrally by GlobalExceptionHandler (`exception` package). JWT
        authentication is enforced by a servlet filter (`security` package).
        Schema is owned by Flyway migrations — Hibernate never mutates the schema
        (`ddl-auto: validate`).
```

Request DTO → entity mapping is done manually in the service layer (request DTOs carry raw FK ids that need ownership-validated lookups anyway); MapStruct `mapper` interfaces only handle entity → response DTO conversion.

### Package layout

```
backend/src/main/java/com/cashpilot
├── controller     # REST endpoints (API entry point)
├── service        # Business rule interfaces
│   └── impl       # Concrete service implementations
├── repository     # Spring Data JPA interfaces
├── entity         # JPA entities (database mapping)
│   └── enums      # Domain enumerations
├── dto
│   ├── request    # Input objects, with Bean Validation
│   └── response   # Output objects
├── mapper         # Entity → response DTO conversion (MapStruct)
├── config         # Configuration (Swagger, CORS, Security)
├── exception      # Custom exceptions + GlobalExceptionHandler
└── security       # JWT authentication filter, JwtService, CurrentUserProvider
```

**Why this structure?** Every layer has a single responsibility. Controllers never talk directly to the database; services never know HTTP details; entities never leak outside the service layer (always via DTO). Every resource query is scoped server-side to the authenticated user's id via `CurrentUserProvider` — there is no admin/role concept, every user only ever sees their own data.

---

## 🧰 Tech stack

| Category               | Technology                            |
|------------------------|------------------------------------------|
| Language                | Java 21                                   |
| Framework               | Spring Boot 3.3                           |
| Web                     | Spring Web (REST)                         |
| Persistence             | Spring Data JPA + Hibernate                |
| Database                | PostgreSQL 16                             |
| Schema migrations       | Flyway                                     |
| Entity ↔ DTO mapping    | MapStruct                                  |
| Validation              | Bean Validation (Jakarta Validation)      |
| Auth                    | Spring Security + JWT (jjwt)               |
| Boilerplate             | Lombok                                     |
| Documentation           | Springdoc OpenAPI / Swagger UI            |
| Testing                 | JUnit 5 + Mockito + AssertJ                |
| Containerization        | Docker + Docker Compose                    |

---

## 🚀 Getting started

### Prerequisites
- Java 21+ (to run locally without Docker)
- Docker and Docker Compose (recommended)

### Option 1 — With Docker (recommended, whole stack)

From the **repository root**:

```bash
docker compose up --build
```

This starts PostgreSQL, the backend API, and the React frontend together. The API comes up at `http://localhost:8080`.

### Option 2 — Backend only, locally with Maven

```bash
# start only the database (from repo root)
docker compose up -d db

# run the application (from backend/)
cd backend
mvn spring-boot:run
```

Flyway runs all `V*__*.sql` migrations under `src/main/resources/db/migration` automatically on startup — there's no manual schema setup step.

### Interactive documentation (Swagger)

With the application running, visit:

```
http://localhost:8080/swagger-ui.html
```

### Running the tests

```bash
cd backend
mvn test
```

Tests are pure unit tests (JUnit 5 + Mockito + AssertJ against mocked repositories, plus a direct test of `ProjectionCalculator`'s math) — no database connection is required to run `mvn test`.

---

## 💰 How balances are computed

`BankAccount.saldoAtual` and `CreditCard.faturaAtual` are **never persisted** — they're calculated on every read. The bank balance is a **realized** balance: only money that has actually moved, up to the day being asked about (`D` = today for `saldoAtual`):

```
saldo(D)    = saldoInicial
            + SUM(Income.valor   WHERE contaBancaria = this account AND recebida AND COALESCE(dataRecebimento, data) <= D)
            - SUM(Expense.valor  WHERE contaBancaria = this account AND paga     AND COALESCE(dataPagamento, data)   <= D)
            - SUM(Transfer.valor WHERE contaOrigem  = this account AND data <= D)
            + SUM(Transfer.valor WHERE contaDestino = this account AND data <= D)

faturaAtual = SUM(Expense.valor WHERE cartaoCredito = this card AND paga = false)
```

Unpaid/unreceived entries and anything dated after `D` are left out; the Cash Flow endpoint is where they show up. `SaldoCalculator` is the single implementation of this formula — `saldoAtual`, the balance history and the snapshots all go through it.

### Balance history and daily snapshots

Because every entry is dated, the balance of any past day can be recomputed. On top of that, `SaldoSnapshotJob` (daily cron at 04:00) stores each account's closing balance of the previous day in `saldos_diarios`, back-filling any days missed while the app was down (up to 730 days). A row is `CAPTURADO` when written right after its day closed and `RECONSTRUIDO` when back-filled later. Snapshots are a historical record, **not** the source of the current balance, and are never rewritten.

The history endpoints return, per day, the recomputed `saldo` plus the stored `saldoRegistrado`/`origem`. When the two differ the point is flagged `divergente` — a transaction for that day was added or edited after the day closed. Today's point never has a snapshot.

Credit-card expenses never reduce a bank account's balance — a card expense has no bank account, so marking it `paga` only clears it from the card's `faturaAtual`. Deleting a `BankAccount`/`CreditCard` is blocked (`BusinessException`, HTTP 422) while it's still referenced by any Income/Expense/Transfer, the same "no raw FK violation" rule used by `Category`.

---

## 📡 Endpoints

Every endpoint below (except `/api/v1/auth/**` and Swagger) requires a valid JWT (`Authorization: Bearer {token}`). There is no role split — every authenticated user has identical privileges over their **own** data only.

### Authentication — `/api/v1/auth`

| Method | Route                    | Description                          |
|--------|--------------------------|-----------------------------------------|
| POST   | `/api/v1/auth/register`  | Self-register a new user                |
| POST   | `/api/v1/auth/login`     | Authenticate and receive a JWT token    |

### Categories — `/api/v1/categorias`

| Method | Route                          | Description                                          |
|--------|----------------------------------|--------------------------------------------------------|
| POST   | `/api/v1/categorias`            | Create a custom category                                |
| PUT    | `/api/v1/categorias/{id}`       | Edit a custom category                                  |
| DELETE | `/api/v1/categorias/{id}`       | Delete a custom category (blocked while in use)         |
| GET    | `/api/v1/categorias/{id}`       | Get a category by id                                    |
| GET    | `/api/v1/categorias`            | List categories (custom + system defaults)              |

### Bank Accounts — `/api/v1/contas`

| Method | Route                     | Description                                  |
|--------|----------------------------|-------------------------------------------------|
| POST   | `/api/v1/contas`           | Create a bank account                           |
| PUT    | `/api/v1/contas/{id}`      | Edit a bank account                              |
| DELETE | `/api/v1/contas/{id}`      | Delete a bank account (blocked while in use)     |
| GET    | `/api/v1/contas/{id}`      | Get a bank account by id (with `saldoAtual`)     |
| GET    | `/api/v1/contas`           | List the user's bank accounts                     |
| GET    | `/api/v1/contas/{id}/historico-saldo` | Daily realized balance of one account for the last N days (`?dias=`, default 30, max 730) |
| POST   | `/api/v1/contas/historico-saldo/capturar` | Manually write the missing daily snapshots for the user's accounts (same logic as the daily cron) |

### Credit Cards — `/api/v1/cartoes`

| Method | Route                      | Description                                     |
|--------|-----------------------------|-----------------------------------------------------|
| POST   | `/api/v1/cartoes`           | Create a credit card                                |
| PUT    | `/api/v1/cartoes/{id}`      | Edit a credit card                                   |
| DELETE | `/api/v1/cartoes/{id}`      | Delete a credit card (blocked while in use)          |
| GET    | `/api/v1/cartoes/{id}`      | Get a credit card by id (with `faturaAtual`)         |
| GET    | `/api/v1/cartoes`           | List the user's credit cards                         |

### Income — `/api/v1/receitas`

| Method | Route                      | Description                                                                  |
|--------|-----------------------------|----------------------------------------------------------------------------------|
| POST   | `/api/v1/receitas`           | Create an income entry                                                          |
| PUT    | `/api/v1/receitas/{id}`      | Edit an income entry                                                              |
| DELETE | `/api/v1/receitas/{id}`      | Delete an income entry                                                            |
| GET    | `/api/v1/receitas/{id}`      | Get an income entry by id                                                          |
| GET    | `/api/v1/receitas`           | List income entries (paginated; optional filters `dataInicio`, `dataFim`, `categoriaId`, `contaId`, `recebida`) |
| PATCH  | `/api/v1/receitas/{id}/receber` | Mark an income entry as received (optional `dataRecebimento` in the body, defaults to today) |

### Expenses — `/api/v1/despesas`

| Method | Route                            | Description                                                                                     |
|--------|------------------------------------|------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/despesas`                 | Create an expense (exactly one of `contaBancariaId`/`cartaoCreditoId`)                               |
| PUT    | `/api/v1/despesas/{id}`            | Edit an expense                                                                                       |
| DELETE | `/api/v1/despesas/{id}`            | Delete an expense                                                                                     |
| GET    | `/api/v1/despesas/{id}`            | Get an expense by id                                                                                   |
| GET    | `/api/v1/despesas`                 | List expenses (paginated; optional filters `dataInicio`, `dataFim`, `categoriaId`, `contaId`, `cartaoId`, `paga`) |
| PATCH  | `/api/v1/despesas/{id}/pagar`      | Mark an expense as paid (optional `dataPagamento` in the body, defaults to today)                     |

### Installment Purchases — `/api/v1/parcelamentos`

Creating an installment purchase automatically generates the expense rows for every installment (splitting `valorTotal` evenly, with the rounding remainder on the last installment). There's no edit — fix a mistake by deleting and re-creating.

| Method | Route                              | Description                                                                                     |
|--------|--------------------------------------|------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/parcelamentos`              | Create an installment purchase (auto-generates the installment expenses)                            |
| DELETE | `/api/v1/parcelamentos/{id}`         | Delete an installment purchase (blocked if any installment is already paid)                          |
| GET    | `/api/v1/parcelamentos/{id}`         | Get an installment purchase by id (with progress: `parcelasPagas`, `valorPago`, `valorRestante`, `quitado`) |
| GET    | `/api/v1/parcelamentos`              | List installment purchases (paginated)                                                               |

### Subscriptions — `/api/v1/assinaturas`

Recurring subscriptions generate monthly expenses automatically (daily cron at 02:00) or on demand via `/gerar-pendentes`. Generation is idempotent per `(assinaturaId, referenciaMes)` — running it twice in the same month never duplicates the charge.

| Method | Route                                   | Description                                                          |
|--------|--------------------------------------------|----------------------------------------------------------------------|
| POST   | `/api/v1/assinaturas`                      | Create a recurring subscription                                       |
| PUT    | `/api/v1/assinaturas/{id}`                 | Edit a recurring subscription                                         |
| DELETE | `/api/v1/assinaturas/{id}`                 | Delete a subscription (already-generated expenses are preserved)      |
| GET    | `/api/v1/assinaturas/{id}`                 | Get a subscription by id                                              |
| GET    | `/api/v1/assinaturas`                      | List the user's subscriptions                                         |
| POST   | `/api/v1/assinaturas/gerar-pendentes`      | Manually generate pending charges for active subscriptions            |

### Cash Flow — `/api/v1/fluxo-caixa`

| Method | Route                       | Description                                                                                                    |
|--------|-------------------------------|----------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/v1/fluxo-caixa`          | Forward-looking balance projection (`?dias=`, default 30): starts from today's realized balance and adds unpaid/unreceived entries, entries dated in the future and not-yet-generated subscription charges. Overdue items keep their original date in the breakdown (`DESPESA_ATRASADA`/`RECEITA_ATRASADA`) and hit the projection today |

### Transfers — `/api/v1/transferencias`

Append-only: you can only create, list and delete — there's no edit.

| Method | Route                              | Description                        |
|--------|--------------------------------------|----------------------------------------|
| POST   | `/api/v1/transferencias`             | Record a transfer between accounts     |
| DELETE | `/api/v1/transferencias/{id}`        | Delete a transfer                       |
| GET    | `/api/v1/transferencias/{id}`        | Get a transfer by id                    |
| GET    | `/api/v1/transferencias`             | List transfers (paginated)              |

### Financial Goals — `/api/v1/metas`

| Method | Route                             | Description                                          |
|--------|-------------------------------------|------------------------------------------------------------|
| POST   | `/api/v1/metas`                     | Create a financial goal                                  |
| PUT    | `/api/v1/metas/{id}`                | Edit a financial goal                                     |
| PATCH  | `/api/v1/metas/{id}/progresso`      | Update the goal's current value (progress)                |
| DELETE | `/api/v1/metas/{id}`                | Delete a financial goal                                    |
| GET    | `/api/v1/metas/{id}`                | Get a financial goal by id (with `progresso` 0-100)        |
| GET    | `/api/v1/metas`                     | List the user's financial goals                            |

### Dashboard — `/api/v1/dashboard`

| Method | Route                              | Description                                                                    |
|--------|--------------------------------------|-------------------------------------------------------------------------------------|
| GET    | `/api/v1/dashboard/resumo`           | Total current balance, month's income/expenses/investments, main goal, upcoming bills |
| GET    | `/api/v1/dashboard/evolucao-saldo`   | Daily realized balance summed across the user's active accounts for the last N days (`?dias=`, default 30, max 730) |

### Projection — `/api/v1/projecao`

| Method | Route                             | Description                                                          |
|--------|-------------------------------------|------------------------------------------------------------------------|
| POST   | `/api/v1/projecao/calcular`         | Calculate when the target net worth will be reached; saves the simulation |
| GET    | `/api/v1/projecao/ultima-simulacao` | Get the user's last saved simulation (404 if none exists)               |

### Reports — `/api/v1/relatorios`

| Method | Route                        | Description                                                                                                   |
|--------|--------------------------------|--------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/v1/relatorios/mensal`     | Monthly report (income, expenses, investments, net balance) for the last N months (`?meses=`, default 12)          |

### Balance Forecast — `/api/v1/previsao-saldo`

| Method | Route                      | Description                                                                                                                             |
|--------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/v1/previsao-saldo`     | Projects the future balance from the historical monthly average net balance (`?mesesHistorico=`, default 6; `?mesesProjecao=`, default 12) |

### Financial Simulation — `/api/v1/simulacoes`

| Method | Route                          | Description                                                                                                              |
|--------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/simulacoes/comparar`     | Compares 2 to 3 scenarios (initial net worth, monthly contribution, monthly return rate) over a horizon in months        |

### Export — `/api/v1/despesas/exportar` and `/api/v1/receitas/exportar`

| Method | Route                        | Description                                                                                                                          |
|--------|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/v1/despesas/exportar`     | Exports filtered expenses as Excel or PDF (`?formato=xlsx\|pdf`, plus the same optional filters as `GET /api/v1/despesas`)               |
| GET    | `/api/v1/receitas/exportar`     | Exports filtered income entries as Excel or PDF (`?formato=xlsx\|pdf`, plus the same optional filters as `GET /api/v1/receitas`)          |

### Family Group — `/api/v1/grupos-familiares`

Lets multiple existing users share the same financial data with roles OWNER (manages membership, can delete the group), MEMBER (full CRUD), and VIEWER (read-only — writes return 403). Joining/leaving never changes a row's actual owner (`user_id`), only the read/write *scope* resolved per request via `CurrentUserProvider.getScopeUserIds()`.

| Method | Route                                              | Description                                                        |
|--------|------------------------------------------------------|-------------------------------------------------------------------------|
| POST   | `/api/v1/grupos-familiares`                          | Create a family group (caller becomes OWNER)                            |
| GET    | `/api/v1/grupos-familiares/me`                       | Get the caller's group + members + role (204 if not in a group)         |
| DELETE | `/api/v1/grupos-familiares`                          | Delete the group (owner only)                                            |
| POST   | `/api/v1/grupos-familiares/convites`                 | Invite an existing user by email + role (owner only)                     |
| GET    | `/api/v1/grupos-familiares/convites/pendentes`       | List pending invites addressed to the caller                             |
| PATCH  | `/api/v1/grupos-familiares/convites/{id}/aceitar`    | Accept an invite                                                          |
| PATCH  | `/api/v1/grupos-familiares/convites/{id}/recusar`    | Decline an invite                                                         |
| PATCH  | `/api/v1/grupos-familiares/membros/{userId}/papel`   | Change a member's role (owner only)                                       |
| DELETE | `/api/v1/grupos-familiares/membros/{userId}`         | Remove a member (owner), or leave the group (self, non-owner)            |

### Notifications — `/api/v1/notificacoes`

In-app alerts for bills due within 3 days, credit card statements closing within 3 days, and goals that reached their target — generated by a daily cron (02:00) and fanned out to every member of the owner's family group, or on demand.

| Method | Route                                     | Description                                                          |
|--------|----------------------------------------------|------------------------------------------------------------------------|
| GET    | `/api/v1/notificacoes`                        | List the caller's notifications (paginated, optional `?lida=` filter)  |
| GET    | `/api/v1/notificacoes/nao-lidas/contagem`     | Count unread notifications                                             |
| PATCH  | `/api/v1/notificacoes/{id}/marcar-lida`       | Mark one notification as read                                          |
| PATCH  | `/api/v1/notificacoes/marcar-todas-lidas`     | Mark all of the caller's notifications as read                         |
| DELETE | `/api/v1/notificacoes/{id}`                   | Delete a notification                                                   |
| POST   | `/api/v1/notificacoes/gerar`                  | Manually trigger generation (same logic as the daily cron)             |

### Open Finance (stub) — `/api/v1/open-finance`

Demonstrates the data model/API shape for a future real Open Finance integration. `/instituicoes` is a hardcoded illustrative list; connecting creates a real `BankAccount`/`CreditCard` row tagged `origem=OPEN_FINANCE`; syncing only bumps a timestamp. No real bank API is ever called and no transaction data is ever fabricated.

| Method | Route                                        | Description                                                              |
|--------|-------------------------------------------------|------------------------------------------------------------------------------|
| GET    | `/api/v1/open-finance/instituicoes`              | List mock institutions available to connect                                  |
| POST   | `/api/v1/open-finance/conectar`                  | Connect a mock institution as a bank account or credit card                  |
| PATCH  | `/api/v1/open-finance/contas/{id}/sincronizar`   | Simulate syncing a connected bank account (updates `ultimaSincronizacao` only) |
| PATCH  | `/api/v1/open-finance/cartoes/{id}/sincronizar`  | Simulate syncing a connected credit card (updates `ultimaSincronizacao` only)  |

---

## 🌱 Commit convention

This project follows **Conventional Commits**:

```
feat: create bank account entity
fix: correct expense payment method validation
refactor: extract dashboard aggregation queries
docs: update readme with endpoints table
style: apply consistent indentation on service layer
test: add unit tests for financial goal service
chore: configure github actions workflow
```

| Type       | When to use                                              |
|------------|-------------------------------------------------------------|
| `feat`     | New feature                                                  |
| `fix`      | Bug fix                                                      |
| `refactor` | Code change without behavior change                          |
| `docs`     | Documentation changes                                         |
| `style`    | Formatting, indentation, no logic change                      |
| `test`     | Adding or adjusting tests                                      |
| `chore`    | Maintenance tasks (build, dependencies, CI, etc.)             |
