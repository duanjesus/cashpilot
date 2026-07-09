import { useMemo, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import { useRelatorioMensal } from "@/hooks/useRelatorios";
import { useExpenses } from "@/hooks/useExpenses";
import { useCategories } from "@/hooks/useCategories";
import { currentMonthRange, formatCurrency } from "@/utils/format";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const FALLBACK_COLORS = ["#3b82f6", "#f97316", "#22c55e", "#ef4444", "#a855f7", "#eab308", "#06b6d4", "#ec4899"];

export function RelatoriosPage() {
  const { data: mensal, isLoading: isMensalLoading, isError: isMensalError } = useRelatorioMensal(12);

  const defaultRange = currentMonthRange();
  const [dataInicio, setDataInicio] = useState(defaultRange.dataInicio);
  const [dataFim, setDataFim] = useState(defaultRange.dataFim);

  const { data: expensesPage, isLoading: isExpensesLoading } = useExpenses({
    dataInicio,
    dataFim,
    size: 200,
  });
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

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Gráficos</h1>
        <p className="text-sm text-slate-500">Relatórios visuais sobre suas finanças.</p>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h2 className="mb-1 font-semibold text-slate-900">Entradas vs. saídas (últimos 12 meses)</h2>
        <p className="mb-3 text-xs text-slate-500">Comparativo mensal de entradas e saídas.</p>
        {isMensalLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isMensalError && <ErrorBanner message="Não foi possível carregar o relatório mensal." />}
        {!isMensalLoading && mensal && mensal.length === 0 && (
          <EmptyState message="Sem dados suficientes para exibir o relatório." />
        )}
        {!isMensalLoading && mensal && mensal.length > 0 && (
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={mensal}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="anoMes" />
              <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
              <Tooltip formatter={(value: number) => formatCurrency(value)} />
              <Legend />
              <Bar dataKey="entradas" name="Entradas" fill="#22c55e" />
              <Bar dataKey="saidas" name="Saídas" fill="#ef4444" />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="font-semibold text-slate-900">Despesas por categoria</h2>
            <p className="text-xs text-slate-500">Selecione o período desejado.</p>
          </div>
          <div className="flex items-center gap-2">
            <Input
              label="De"
              type="date"
              value={dataInicio}
              onChange={(e) => setDataInicio(e.target.value)}
            />
            <Input label="Até" type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
          </div>
        </div>
        {isExpensesLoading ? (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        ) : expensesByCategory.length === 0 ? (
          <EmptyState message="Nenhuma despesa registrada neste período." />
        ) : (
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={expensesByCategory} dataKey="valor" nameKey="nome" outerRadius={100} label>
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
  );
}
