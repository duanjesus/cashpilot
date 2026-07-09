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
│   └── ...           # categorias, contas, cartoes, receitas, despesas, parcelamentos, assinaturas,
│                      # contas-a-pagar, contas-a-receber, transferencias, metas
├── types/           # TypeScript types mirroring the backend DTOs
└── utils/           # Formatting helpers (dates, currency, percentages)
```

## Authentication

The app stores the JWT returned by `/api/v1/auth/login` (or `/register`, which also logs in) in `localStorage` under `cashpilot.token`/`cashpilot.user`. Every request attaches `Authorization: Bearer <token>` via an Axios request interceptor. A 401 response clears the session and redirects to `/login`.

CashPilot has **no roles or admin/operator split** — every authenticated user only ever sees and manages their own data, enforced server-side. There is no equivalent of an `AdminRoute`; `<ProtectedRoute>` is the only route guard.

## Notes on the dashboard charts

- **Entradas vs. saídas**: compares the current month's totals from `/dashboard/resumo` as a two-bar chart. A true 6-month trailing history would need a dedicated backend endpoint that doesn't exist yet in this contract, so this is a deliberate simplification for V1.
- **Despesas por categoria**: fetched client-side via `/despesas` filtered to the current month's date range, then aggregated by `categoriaNome`; slice colors use each category's `cor` field where available, falling back to a fixed palette.
- **Tendência de fluxo de caixa**: sourced from `/dashboard/evolucao-saldo`. This is explicitly labeled in the UI as an *approximate* net cash-flow trend, not an audited balance history, since V1 stores no historical balance snapshots.
