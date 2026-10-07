# CLAUDE.md

Guidance for Claude Code (or any AI coding agent) working in this repository.

## What this is

A monorepo for CashPilot, a personal financial management platform: users track **bank accounts** and **credit cards**, record **income** and **expenses** against them, move money between their own accounts via **transfers**, track **financial goals**, and get a **financial projection** of when they'll reach a target net worth given their current savings rate.

```
cashpilot/
├── backend/    Spring Boot 3 REST API (Java 21, PostgreSQL, Flyway migrations, MapStruct, JWT auth)
├── frontend/   React + TypeScript SPA (Vite, Tailwind, TanStack Query, Recharts)
├── docker-compose.yml   Orchestrates db + api + web
└── .github/workflows/ci.yml   Two jobs: backend (Maven), frontend (npm)
```

Each package has its own `README.md` with full details — [backend/README.md](backend/README.md), [frontend/README.md](frontend/README.md). This file focuses on cross-cutting context and conventions an agent needs before making changes.

## Running things

```bash
cd frontend && npm ci && npm run lint && npm run build   # or: npm run dev
cd backend && mvn -B clean test                            # or: mvn spring-boot:run
```

`frontend/package-lock.json` is committed — CI uses `npm ci` with the lockfile cached. If you add/bump a dependency, run `npm install` locally and commit the updated lockfile.

Full stack via Docker (from repo root): `docker compose up --build` — frontend at `:3000`, API at `:8080`, Swagger at `:8080/swagger-ui.html`. Both `docker-compose.yml` here and the sibling `social-supply-management-api` repo use fixed container names (`cashpilot-db`/`cashpilot-api`/`cashpilot-web` vs `doacoes-*`) so the two stacks don't collide, but running two checkouts of *this* repo at once will still collide on container names and the shared ports (5432/8080/3000).

## The contract between frontend and backend

The frontend has **no backend code of its own** — it's a pure client of the API. When you change a backend DTO, controller route, enum, or validation rule, the matching frontend type/hook/form almost certainly needs updating too, and vice versa. Check both sides.

| Backend source of truth | Frontend mirror |
|---|---|
| `backend/src/main/java/.../dto/request/*.java` | `frontend/src/types/*.ts` (`*Request` interfaces) |
| `backend/src/main/java/.../dto/response/*.java` | `frontend/src/types/*.ts` (entity interfaces) |
| `backend/src/main/java/.../entity/enums/*.java` | `frontend/src/types/*.ts` (union types + `*_LABELS` maps) |
| `backend/src/main/java/.../controller/*.java` | `frontend/src/hooks/use*.ts` (route paths, query params) |
| Bean Validation annotations on request DTOs | Zod schemas in `frontend/src/pages/**/*FormModal.tsx` |
| `GlobalExceptionHandler` → `ErrorResponse` shape | `frontend/src/types/common.ts` (`ApiErrorResponse`) + `lib/api.ts` (`extractErrorMessage`) |

Domain enums as of this writing — check the Java source before relying on this, it will drift:
- `CategoryType`: `RECEITA`, `DESPESA`, `AMBOS`
- `BankAccountType`: `CORRENTE`, `POUPANCA`, `CARTEIRA`, `OUTRA`
- `CardBrand`: `VISA`, `MASTERCARD`, `ELO`, `AMEX`, `OUTRA`

API base path: `/api/v1`. All routes require a JWT (`Authorization: Bearer <token>`) except `/api/v1/auth/**` and Swagger. There is no admin/operator role split (unlike the sibling project's ADMIN/OPERATOR). Since V4, a user may optionally belong to a **family group** with a per-group role — OWNER (manages membership, can delete the group), MEMBER (full CRUD), VIEWER (read-only, writes get a 403) — but a solo user (no group) still has full, identical privileges over their own data, same as before. Every resource query is scoped server-side via `CurrentUserProvider.getScopeUserIds()` (`backend/src/main/java/com/cashpilot/security/CurrentUserProvider.java`) — returns just the caller's own id when solo, or every group member's id when grouped; row ownership (the `user_id` FK) never changes on joining/leaving a group, only this read/write scope does. Mutating services call `currentUserProvider.requireWriteAccess()` first to block VIEWERs. Never trust a client-supplied `userId`, and never add an endpoint that returns a row outside the caller's resolved scope.

**Financial balances are computed on read, never stored.** `BankAccount.saldoAtual` and `CreditCard.faturaAtual` are calculated in the service layer from `saldoInicial`/transactions at request time, not persisted columns — see `backend/README.md` for the exact formula. The bank balance is a *realized* balance: only received income, paid expenses and transfers dated up to the day in question count, and `SaldoCalculator` is the only place that formula lives — never re-derive a balance with an ad-hoc sum. Credit-card expenses never reduce a bank account's balance; a card expense has no bank account, so marking it `paga` only clears it from the card's `faturaAtual`. If you add a new kind of transaction, make sure it's included in (or deliberately excluded from) `SaldoCalculator`, the Cash Flow "not yet realized" queries, and the equivalent dashboard aggregates.

**Daily balance snapshots (`saldos_diarios`) are a historical record, not the source of the balance.** `SaldoSnapshotJob` writes one row per account per closed day and never rewrites one; the history endpoints always return the recomputed balance and flag a day as `divergente` when its snapshot no longer matches. Don't "fix" a divergence by updating the snapshot — it is the evidence that a transaction changed after the fact.

Transfers and Distributions/Donations-style append-only resources: **Transfers have no `PUT` endpoint** (`POST`/`GET`/`DELETE` only) — mistakes are corrected by deleting and re-entering, never by silently editing an amount. Don't add an edit UI or endpoint for transfers without deliberately revisiting this.

Categories are a shared taxonomy: system defaults (`user_id IS NULL`, seeded via Flyway `V10__seed_default_categorias.sql`) plus user-created custom ones. System categories are read-only (can't be edited/deleted by any user); a user's own category can't be deleted while referenced by any Income/Expense (checked in `CategoryServiceImpl`, not left to a raw FK violation).

## Backend conventions (`backend/`)

- Layered architecture: `controller` → `service` (+ `service/impl`) → `repository`, with MapStruct `mapper` interfaces for entity→response DTO conversion only. Request DTO → entity is built manually in the service (request DTOs carry raw FK ids that need ownership-validated lookups anyway — MapStruct wouldn't save real work there). Controllers never touch repositories directly; entities never leave the service layer.
- Request DTOs are Java `record`s with Bean Validation annotations (`@NotBlank`, `@Size`, etc.) — validation messages are what `GlobalExceptionHandler` surfaces to the client field-by-field.
- Custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `BusinessException`, `InvalidCredentialsException`) map to specific HTTP statuses in `GlobalExceptionHandler` — throw the right one rather than a generic exception.
- Schema is owned by **Flyway** (`backend/src/main/resources/db/migration/V*.sql`), `ddl-auto: validate` — Hibernate never mutates the schema. Adding a column/table means a new `V<n>__description.sql` migration, never editing an already-applied one.
- JWT is HS256, secret and expiration come from `jwt.secret` / `jwt.expiration-ms` (env `JWT_SECRET` / `JWT_EXPIRATION_MS`), see `security/JwtService.java`. `CurrentUserProvider` resolves the authenticated `User` entity from the security context — use it in every service that owns per-user data.
- Tests live in `backend/src/test`, using JUnit 5 + Mockito + AssertJ, one test class per service impl. The `ProjectionCalculator` math is tested directly (no mocks) since it's a pure function.
- Commit convention: Conventional Commits (`feat`, `fix`, `refactor`, `docs`, `style`, `test`, `chore`).

## Frontend conventions (`frontend/`)

- Vite + React 18 + TypeScript, path alias `@/*` → `frontend/src/*`.
- Server state (all API data) goes through **TanStack Query** hooks in `src/hooks/` — one file per resource, exporting query hooks and mutation hooks. Don't call `api` directly from a page component; add/extend a hook instead.
- Forms use **React Hook Form + Zod**, with a Zod schema colocated in the `*FormModal.tsx` file, deliberately mirroring the backend's Bean Validation constraints. If the backend validation changes, update the schema.
- `src/lib/api.ts` holds the single Axios instance: request interceptor attaches the JWT from `localStorage` (`cashpilot.token`/`cashpilot.user` keys), response interceptor clears the session and redirects to `/login` on 401. `extractErrorMessage(error)` turns a backend `ErrorResponse` into a user-facing string — use it in every mutation's `catch`.
- Auth/session state lives in `src/context/AuthContext.tsx` (`useAuth()` gives `user`, `isAuthenticated`, `login`, `register`, `logout`) — there's no `isAdmin`/role concept here.
- `<ProtectedRoute>` wraps every authenticated page, composed in `App.tsx`.
- Reusable primitives live in `src/components/ui/` (`Button`, `Input`, `Select`, `Textarea`, `Modal`, `Pagination`, `Badge`, `Spinner`, `EmptyState`, `ErrorBanner`) — prefer these over ad-hoc markup.
- Page structure per resource: `src/pages/<resource>/<Resource>ListPage.tsx` (table + pagination/list) and `<Resource>FormModal.tsx` (create/edit form in a modal). Transfers have no edit form (append-only, matches the backend). The Dashboard and Projection pages don't follow this pattern — they're single-view pages, not CRUD lists.
- Styling is Tailwind utility classes only. The `brand` color scale is defined in `tailwind.config.js`.
- Dashboard charts use **Recharts**. The balance-evolution line chart and the per-account history modal share `components/SaldoHistoricoChart.tsx`, which plots the recomputed realized balance and marks days whose stored snapshot diverges.
- In dev, Vite proxies `/api/*` to `http://localhost:8080`; in the Docker image, nginx does the same — the frontend always calls a relative `/api/v1` and never hardcodes a host.

## Things to watch for

- Money fields are `BigDecimal` on the backend — don't introduce floating point for currency anywhere.
- Dates are ISO `yyyy-MM-dd` strings end-to-end (`LocalDate` on the backend).
- CORS is wide open (`allowedOriginPatterns("*")` in `backend/.../config/WebConfig.java`) — fine for this project's current scope, revisit before adding real authz stakes.
- Expense's payment method is an XOR: exactly one of `contaBancariaId`/`cartaoCreditoId` must be set — enforced in the request DTO validation and the service, backed by a DB `CHECK` constraint as a last line of defense. Don't relax this without updating all three layers.
