import { useMemo } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import { useBalanceEvolution, useDashboardSummary } from "@/hooks/useDashboard";
import { useExpenses } from "@/hooks/useExpenses";
import { useCategories } from "@/hooks/useCategories";
import { currentMonthRange, formatCurrency, formatDate, formatPercent } from "@/utils/format";
import { Spinner } from "@/components/ui/Spinner";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { EmptyState } from "@/components/ui/EmptyState";

const FALLBACK_COLORS = ["#3b82f6", "#f97316", "#22c55e", "#ef4444", "#a855f7", "#eab308", "#06b6d4", "#ec4899"];

function StatTile({ label, value, tone }: { label: string; value: string; tone?: "green" | "red" | "default" }) {
  const toneClass = tone === "green" ? "text-green-700" : tone === "red" ? "text-red-700" : "text-slate-900";
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-2xl font-semibold ${toneClass}`}>{value}</p>
    </div>
  );
}

export function DashboardPage() {
  const { data: summary, isLoading: isSummaryLoading, isError: isSummaryError } = useDashboardSummary();
  const { data: evolution, isLoading: isEvolutionLoading } = useBalanceEvolution(30);
  const { dataInicio, dataFim } = currentMonthRange();
  const { data: expensesPage, isLoading: isExpensesLoading } = useExpenses({ dataInicio, dataFim, size: 200 });
  const { data: categories } = useCategories();

  const categoryColorByName = useMemo(() => {
    const map = new Map<string, string | null>();
    categories?.forEach((category) => map.set(category.nome, category.cor));
    return map;
  }, [categories]);

  const expensesByCategory = useMemo(() => {
    if (!expensesPage) return [];
    const totals = new Map<string, number>();
    expensesPage.content.forEach((expense) => {
      totals.set(expense.categoriaNome, (totals.get(expense.categoriaNome) ?? 0) + expense.valor);
    });
    return Array.from(totals.entries()).map(([nome, valor]) => ({ nome, valor }));
  }, [expensesPage]);

  const barData = summary
    ? [{ nome: "Este mês", entradas: summary.entradasMes, saidas: summary.saidasMes }]
    : [];

  if (isSummaryLoading) {
    return (
      <div className="flex justify-center p-10">
        <Spinner />
      </div>
    );
  }

  if (isSummaryError || !summary) {
    return <ErrorBanner message="Não foi possível carregar o resumo do dashboard." />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Visão geral</h1>
        <p className="text-sm text-slate-500">Resumo das suas finanças.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatTile label="Saldo atual" value={formatCurrency(summary.saldoAtual)} />
        <StatTile label="Entradas do mês" value={formatCurrency(summary.entradasMes)} tone="green" />
        <StatTile label="Saídas do mês" value={formatCurrency(summary.saidasMes)} tone="red" />
        <StatTile label="Investimentos do mês" value={formatCurrency(summary.investimentosMes)} />
      </div>

      {summary.metaPrincipal && (
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-slate-900">Meta principal: {summary.metaPrincipal.nome}</h2>
            <span className="text-sm text-slate-500">até {formatDate(summary.metaPrincipal.dataAlvo)}</span>
          </div>
          <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-slate-100">
            <div
              className="h-full rounded-full bg-brand-600"
              style={{ width: `${Math.min(100, Math.max(0, summary.metaPrincipal.progresso))}%` }}
            />
          </div>
          <div className="mt-1 text-sm text-slate-600">
            {formatCurrency(summary.metaPrincipal.valorAtual)} de {formatCurrency(summary.metaPrincipal.valorAlvo)} (
            {formatPercent(summary.metaPrincipal.progresso)})
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 className="mb-1 font-semibold text-slate-900">Entradas vs. saídas</h2>
          <p className="mb-3 text-xs text-slate-500">Comparativo do mês atual.</p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="nome" />
              <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
              <Tooltip formatter={(value: number) => formatCurrency(value)} />
              <Legend />
              <Bar dataKey="entradas" name="Entradas" fill="#22c55e" />
              <Bar dataKey="saidas" name="Saídas" fill="#ef4444" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 className="mb-1 font-semibold text-slate-900">Despesas por categoria (mês atual)</h2>
          {isExpensesLoading ? (
            <div className="flex justify-center p-10">
              <Spinner />
            </div>
          ) : expensesByCategory.length === 0 ? (
            <EmptyState message="Nenhuma despesa registrada neste mês." />
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={expensesByCategory} dataKey="valor" nameKey="nome" outerRadius={90} label>
                  {expensesByCategory.map((entry, index) => (
                    <Cell
                      key={entry.nome}
                      fill={categoryColorByName.get(entry.nome) || FALLBACK_COLORS[index % FALLBACK_COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip formatter={(value: number) => formatCurrency(value)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h2 className="mb-1 font-semibold text-slate-900">Tendência de fluxo de caixa (aproximada)</h2>
        <p className="mb-3 text-xs text-slate-500">
          Esta linha é uma aproximação do fluxo de caixa acumulado nos últimos 30 dias, calculada a partir de
          receitas e despesas — não é um histórico auditado de saldo.
        </p>
        {isEvolutionLoading ? (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        ) : !evolution || evolution.length === 0 ? (
          <EmptyState message="Sem dados suficientes para exibir a tendência." />
        ) : (
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={evolution}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="data" tickFormatter={(v: string) => formatDate(v)} />
              <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
              <Tooltip
                labelFormatter={(v) => formatDate(String(v))}
                formatter={(value: number) => formatCurrency(value)}
              />
              <Line type="monotone" dataKey="valorAcumulado" name="Fluxo acumulado" stroke="#2563eb" dot={false} />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 px-4 py-3">
          <h2 className="font-semibold text-slate-900">Próximas contas</h2>
        </div>
        {summary.proximasContas.length === 0 ? (
          <EmptyState message="Nenhuma conta prevista para os próximos dias." />
        ) : (
          <ul className="divide-y divide-slate-100">
            {summary.proximasContas.map((bill) => (
              <li key={bill.id} className="flex items-center justify-between px-4 py-3 text-sm">
                <div>
                  <p className="font-medium text-slate-900">{bill.descricao}</p>
                  <p className="text-xs text-slate-500">
                    {bill.categoriaNome} • {formatDate(bill.data)}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-medium text-slate-900">{formatCurrency(bill.valor)}</p>
                  <p className="text-xs text-slate-500">em {bill.diasRestantes} dia(s)</p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
