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

`BankAccount.saldoAtual` and `CreditCard.faturaAtual` are **never persisted** — they're calculated on every read from `saldoInicial` plus the relevant transactions:

```
saldoAtual = saldoInicial
           + SUM(Income.valor WHERE contaBancaria = esta conta)
           - SUM(Expense.valor WHERE contaBancaria = esta conta)
           - SUM(Transfer.valor WHERE contaOrigem = esta conta)
           + SUM(Transfer.valor WHERE contaDestino = esta conta)

faturaAtual = SUM(Expense.valor WHERE cartaoCredito = este cartão AND paga = false)
```

Credit-card expenses never reduce a bank account's balance in V1 — only paying off the card (or manually marking the expense `paga`) does. Deleting a `BankAccount`/`CreditCard` is blocked (`BusinessException`, HTTP 422) while it's still referenced by any Income/Expense/Transfer, the same "no raw FK violation" rule used by `Category`.

---

## 📡 Endpoints

Every endpoint below (except `/api/v1/auth/**` and Swagger) requires a valid JWT (`Authorization: Bearer {token}`). There is no role split — every authenticated user has identical privileges over their **own** data only.

### Authentication — `/api/v1/auth`

| Method | Route                    | Description                          |
|--------|--------------------------|-----------------------------------------|
| POST   | `/api/v1/auth/register`  | Self-register a new user                |
| POST   | `/api/v1/auth/login`     | Authenticate and receive a JWT token    |

### Categorias — `/api/v1/categorias`

| Method | Route                          | Description                                          |
|--------|----------------------------------|--------------------------------------------------------|
| POST   | `/api/v1/categorias`            | Cadastrar categoria própria                             |
| PUT    | `/api/v1/categorias/{id}`       | Editar categoria própria                                |
| DELETE | `/api/v1/categorias/{id}`       | Excluir categoria própria (bloqueado se em uso)         |
| GET    | `/api/v1/categorias/{id}`       | Buscar categoria por ID                                 |
| GET    | `/api/v1/categorias`            | Listar categorias (próprias + padrão do sistema)        |

### Contas Bancárias — `/api/v1/contas`

| Method | Route                     | Description                                  |
|--------|----------------------------|-------------------------------------------------|
| POST   | `/api/v1/contas`           | Cadastrar conta bancária                        |
| PUT    | `/api/v1/contas/{id}`      | Editar conta bancária                            |
| DELETE | `/api/v1/contas/{id}`      | Excluir conta bancária (bloqueado se em uso)     |
| GET    | `/api/v1/contas/{id}`      | Buscar conta bancária por ID (com `saldoAtual`)  |
| GET    | `/api/v1/contas`           | Listar contas bancárias do usuário                |

### Cartões de Crédito — `/api/v1/cartoes`

| Method | Route                      | Description                                     |
|--------|-----------------------------|-----------------------------------------------------|
| POST   | `/api/v1/cartoes`           | Cadastrar cartão de crédito                        |
| PUT    | `/api/v1/cartoes/{id}`      | Editar cartão de crédito                            |
| DELETE | `/api/v1/cartoes/{id}`      | Excluir cartão de crédito (bloqueado se em uso)     |
| GET    | `/api/v1/cartoes/{id}`      | Buscar cartão de crédito por ID (com `faturaAtual`) |
| GET    | `/api/v1/cartoes`           | Listar cartões de crédito do usuário                 |

### Receitas — `/api/v1/receitas`

| Method | Route                      | Description                                                                  |
|--------|-----------------------------|----------------------------------------------------------------------------------|
| POST   | `/api/v1/receitas`           | Cadastrar receita                                                                |
| PUT    | `/api/v1/receitas/{id}`      | Editar receita                                                                    |
| DELETE | `/api/v1/receitas/{id}`      | Excluir receita                                                                    |
| GET    | `/api/v1/receitas/{id}`      | Buscar receita por ID                                                              |
| GET    | `/api/v1/receitas`           | Listar receitas (paginado; filtros opcionais `dataInicio`, `dataFim`, `categoriaId`, `contaId`, `recebida`) |
| PATCH  | `/api/v1/receitas/{id}/receber` | Marcar receita como recebida (`dataRecebimento` opcional no corpo, padrão hoje)             |

### Despesas — `/api/v1/despesas`

| Method | Route                            | Description                                                                                     |
|--------|------------------------------------|------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/despesas`                 | Cadastrar despesa (exatamente uma de `contaBancariaId`/`cartaoCreditoId`)                            |
| PUT    | `/api/v1/despesas/{id}`            | Editar despesa                                                                                        |
| DELETE | `/api/v1/despesas/{id}`            | Excluir despesa                                                                                        |
| GET    | `/api/v1/despesas/{id}`            | Buscar despesa por ID                                                                                  |
| GET    | `/api/v1/despesas`                 | Listar despesas (paginado; filtros opcionais `dataInicio`, `dataFim`, `categoriaId`, `contaId`, `cartaoId`, `paga`) |
| PATCH  | `/api/v1/despesas/{id}/pagar`      | Marcar despesa como paga (`dataPagamento` opcional no corpo, padrão hoje)                             |

### Parcelamentos — `/api/v1/parcelamentos`

Cadastrar um parcelamento gera automaticamente as despesas de todas as parcelas (dividindo `valorTotal` igualmente, com o resto de arredondamento na última parcela). Não há edição — corrija excluindo e recadastrando.

| Method | Route                              | Description                                                                                     |
|--------|--------------------------------------|------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/parcelamentos`              | Cadastrar parcelamento (gera as despesas das parcelas automaticamente)                              |
| DELETE | `/api/v1/parcelamentos/{id}`         | Excluir parcelamento (bloqueado se houver parcelas já pagas)                                          |
| GET    | `/api/v1/parcelamentos/{id}`         | Buscar parcelamento por ID (com progresso: `parcelasPagas`, `valorPago`, `valorRestante`, `quitado`) |
| GET    | `/api/v1/parcelamentos`              | Listar parcelamentos (paginado)                                                                       |

### Assinaturas — `/api/v1/assinaturas`

Assinaturas recorrentes geram despesas mensais automaticamente (cron diário, 02:00) ou sob demanda via `/gerar-pendentes`. A geração é idempotente por `(assinaturaId, referenciaMes)` — rodar duas vezes no mesmo mês nunca duplica a cobrança.

| Method | Route                                   | Description                                                          |
|--------|--------------------------------------------|----------------------------------------------------------------------|
| POST   | `/api/v1/assinaturas`                      | Cadastrar assinatura recorrente                                       |
| PUT    | `/api/v1/assinaturas/{id}`                 | Editar assinatura recorrente                                          |
| DELETE | `/api/v1/assinaturas/{id}`                 | Excluir assinatura (despesas já geradas são preservadas)              |
| GET    | `/api/v1/assinaturas/{id}`                 | Buscar assinatura por ID                                              |
| GET    | `/api/v1/assinaturas`                      | Listar assinaturas do usuário                                         |
| POST   | `/api/v1/assinaturas/gerar-pendentes`      | Gerar manualmente as cobranças pendentes das assinaturas ativas       |

### Fluxo de Caixa — `/api/v1/fluxo-caixa`

| Method | Route                       | Description                                                                                                    |
|--------|-------------------------------|----------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/v1/fluxo-caixa`          | Projeção de saldo futuro (`?dias=`, padrão 30) combinando despesas/receitas pendentes e cobranças de assinaturas ainda não geradas |

### Transferências — `/api/v1/transferencias`

Append-only: só é possível registrar, listar e excluir — não há edição.

| Method | Route                              | Description                        |
|--------|--------------------------------------|----------------------------------------|
| POST   | `/api/v1/transferencias`             | Registrar transferência entre contas   |
| DELETE | `/api/v1/transferencias/{id}`        | Excluir transferência                   |
| GET    | `/api/v1/transferencias/{id}`        | Buscar transferência por ID             |
| GET    | `/api/v1/transferencias`             | Listar transferências (paginado)        |

### Metas Financeiras — `/api/v1/metas`

| Method | Route                             | Description                                          |
|--------|-------------------------------------|----------------------------------------------------------|
| POST   | `/api/v1/metas`                     | Cadastrar meta financeira                                |
| PUT    | `/api/v1/metas/{id}`                | Editar meta financeira                                    |
| PATCH  | `/api/v1/metas/{id}/progresso`      | Atualizar o valor atual (progresso) da meta               |
| DELETE | `/api/v1/metas/{id}`                | Excluir meta financeira                                    |
| GET    | `/api/v1/metas/{id}`                | Buscar meta financeira por ID (com `progresso` 0–100)      |
| GET    | `/api/v1/metas`                     | Listar metas financeiras do usuário                        |

### Dashboard — `/api/v1/dashboard`

| Method | Route                              | Description                                                                    |
|--------|--------------------------------------|-------------------------------------------------------------------------------------|
| GET    | `/api/v1/dashboard/resumo`           | Saldo atual total, entradas/saídas/investimentos do mês, meta principal, próximas contas |
| GET    | `/api/v1/dashboard/evolucao-saldo`   | Série de fluxo de caixa acumulado (aproximação) dos últimos N dias (`?dias=`)         |

### Projeção — `/api/v1/projecao`

| Method | Route                             | Description                                                          |
|--------|-------------------------------------|----------------------------------------------------------------------|
| POST   | `/api/v1/projecao/calcular`         | Calcular quando o patrimônio alvo será atingido; salva a simulação    |
| GET    | `/api/v1/projecao/ultima-simulacao` | Buscar a última simulação salva do usuário (404 se nunca simulou)     |

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
