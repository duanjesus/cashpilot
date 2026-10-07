# CashPilot — Frontend

React + TypeScript single-page app that consumes the [backend API](../backend) to manage bank accounts, credit cards, income, expenses, transfers, financial goals, and net-worth projections for a single user's personal finances.

> This is the frontend half of the [CashPilot monorepo](../README.md).

## Tech stack

- [Vite](https://vitejs.dev/) + React 18 + TypeScript
- [React Router](https://reactrouter.com/) for client-side routing
- [TanStack Query](https://tanstack.com/query) for server-state (fetching, caching, invalidation)
- [React Hook Form](https://react-hook-form.com/) + [Zod](https://zod.dev/) for forms and validation
- [Axios](https://axios-http.com/) for HTTP, with a JWT interceptor
- [Tailwind CSS](https://tailwindcss.com/) for styling
- [Recharts](https://recharts.org/) for the dashboard charts

## Getting started

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

The app runs at `http://localhost:5173`. In dev mode, Vite proxies any `/api/*` request to `http://localhost:8080` (see `vite.config.ts`), so the backend must be running separately (`docker compose up -d db api` from the repo root, or `mvn spring-boot:run` inside `backend/`).

## Scripts

| Command           | Description                          |
|--------------------|---------------------------------------|
| `npm run dev`      | Start the Vite dev server             |
| `npm run build`    | Type-check and build for production   |
| `npm run lint`     | Run ESLint                            |
| `npm run preview`  | Preview the production build locally  |

## Structure

```
src/
├── components/
│   ├── layout/      # AppLayout, Sidebar, Header
│   └── ui/          # Button, Input, Select, Modal, Pagination, etc.
├── context/         # AuthContext (JWT session, current user)
├── hooks/           # TanStack Query hooks per resource (categories, accounts, incomes, ...)
├── lib/             # Axios instance + interceptors, QueryClient
├── pages/           # One folder per resource, each with a list page + form modal
│   ├── auth/        # LoginPage, RegisterPage
│   ├── dashboard/    # DashboardPage (stat tiles + Recharts charts)
│   ├── projecao/     # ProjectionPage (net-worth projection calculator)
│   ├── fluxo-caixa/  # FluxoCaixaPage (forward-looking projected balance chart + breakdown)
│   ├── relatorios/   # RelatoriosPage (entradas vs. saídas bar chart + despesas por categoria pie chart)
│   ├── previsao-saldo/ # PrevisaoSaldoPage (long-term projected balance line chart, based on historical averages)
│   ├── simulacao/    # SimulacaoPage (compare 2-3 what-if financial scenarios side by side)
│   ├── grupo-familiar/ # GrupoFamiliarPage (create/join a family group, invite/manage members and roles)
│   ├── notificacoes/ # NotificacoesPage (paginated notification history)
│   ├── open-finance/ # OpenFinancePage (connect mock institutions, sync stub)
│   └── ...           # categorias, contas, cartoes, receitas, despesas, parcelamentos, assinaturas,
│                      # contas-a-pagar, contas-a-receber, transferencias, metas
├── types/           # TypeScript types mirroring the backend DTOs
└── utils/           # Formatting helpers (dates, currency, percentages)
```

## Authentication

The app stores the JWT returned by `/api/v1/auth/login` (or `/register`, which also logs in) in `localStorage` under `cashpilot.token`/`cashpilot.user`. Every request attaches `Authorization: Bearer <token>` via an Axios request interceptor. A 401 response clears the session and redirects to `/login`.

CashPilot has **no admin/operator split** — every authenticated user only ever sees and manages their own data by default. Since V4, a user can optionally join a family group with an OWNER/MEMBER/VIEWER role (see below); there is still no equivalent of an `AdminRoute`, `<ProtectedRoute>` is the only route guard, and VIEWER write-blocking is enforced server-side (403) — the frontend only hides a few obvious create buttons as a UX nicety via `useMyFamilyGroup()`.

## Notes on the dashboard charts

- **Income vs. expenses**: compares the current month's totals from `/dashboard/resumo` as a two-bar chart. A true 6-month trailing history would need a dedicated backend endpoint that doesn't exist yet in this contract, so this is a deliberate simplification for V1.
- **Expenses by category**: fetched client-side via `/despesas` filtered to the current month's date range, then aggregated by `categoriaNome`; slice colors use each category's `cor` field where available, falling back to a fixed palette.
- **Balance history**: sourced from `/dashboard/evolucao-saldo` and rendered by the shared `SaldoHistoricoChart` (also used by the per-account history modal on the accounts page, fed by `/contas/{id}/historico-saldo`). The line is the realized balance recomputed from current transactions; days whose stored snapshot no longer matches it get an amber marker and the tooltip shows the balance recorded at the time.

## Notes on the V3 analytics pages

- **Charts** (`/relatorios`): a 12-month income vs. expenses bar chart from `/relatorios/mensal`, plus an expenses-by-category pie chart with a user-selectable date range (fetched client-side via `/despesas`, same aggregation technique as the dashboard's pie).
- **Balance forecast** (`/previsao-saldo`): a long-term balance projection line chart from `/previsao-saldo`, based on the historical average of income/expenses over the last N months. This is explicitly distinguished in the UI from **Cash Flow**, which only projects already-known near-term items (pending expenses/incomes/subscriptions) — the two pages answer different questions and shouldn't be confused.
- **Simulation** (`/simulacao`): compares 2-3 user-defined what-if scenarios (initial net worth, monthly contribution, monthly return rate) over a shared horizon via `POST /simulacoes/comparar`, rendered as a multi-series line chart.
- **Export**: `DespesasListPage` and `ReceitasListPage` each have "Export to Excel"/"Export to PDF" buttons that download the currently filtered list from `/despesas/exportar` or `/receitas/exportar` (see `utils/download.ts`'s `downloadBlob` helper — needed because authenticated file downloads can't use a plain `<a href>`).

## Notes on the V4 pages

- **Grupo Familiar** (`/grupo-familiar`): single-view page (like Dashboard/Projection, not a list+modal CRUD page) — shows a "create group" CTA when the user isn't in one, or the group's members/roles/invite form when they are. `useMyFamilyGroup()` also drives the small VIEWER-role CTA-hiding on the Despesas/Receitas/Contas list pages.
- **Notificações** (`/notificacoes` + the bell icon in `Header`): the bell polls `/notificacoes/nao-lidas/contagem` every 30s for its badge and shows the 5 most recent notifications in a dropdown, with a "Gerar agora" button for demoing without waiting for the daily cron; the full page adds pagination and a read/unread filter.
- **Open Finance** (`/open-finance`): a clearly-labeled demo page — connecting a mock institution creates a real (zero-balance) `BankAccount`/`CreditCard` tagged `origem: "OPEN_FINANCE"`, which then also shows an "Open Finance" badge on the regular Contas/Cartões list pages.
