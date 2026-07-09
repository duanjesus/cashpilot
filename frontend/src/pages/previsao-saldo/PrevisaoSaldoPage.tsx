import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { usePrevisaoSaldo } from "@/hooks/usePrevisaoSaldo";
import { formatCurrency } from "@/utils/format";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

function StatTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-2xl font-semibold text-slate-900">{value}</p>
    </div>
  );
}

export function PrevisaoSaldoPage() {
  const mesesHistorico = 6;
  const { data, isLoading, isError } = usePrevisaoSaldo(mesesHistorico, 12);

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Previsão de saldo</h1>
        <p className="text-sm text-slate-500">
          Projeção de longo prazo do seu saldo com base na média histórica de entradas e saídas.
        </p>
      </div>

      <div className="rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-800">
        Esta é uma projeção de longo prazo baseada na média histórica de entradas e saídas dos últimos{" "}
        {mesesHistorico} meses — diferente do Fluxo de Caixa, que projeta apenas valores já conhecidos (despesas,
        receitas e assinaturas pendentes) no curto prazo.
      </div>

      {isLoading && (
        <div className="flex justify-center p-10">
          <Spinner />
        </div>
      )}
      {isError && <ErrorBanner message="Não foi possível carregar a previsão de saldo." />}

      {!isLoading && data && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <StatTile label="Saldo atual" value={formatCurrency(data.saldoAtual)} />
            <StatTile label="Média mensal histórica" value={formatCurrency(data.mediaMensalHistorica)} />
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h2 className="mb-1 font-semibold text-slate-900">Saldo projetado</h2>
            <p className="mb-3 text-xs text-slate-500">Projeção mês a mês para os próximos meses.</p>
            {data.serie.length === 0 ? (
              <EmptyState message="Sem dados suficientes para projetar o saldo." />
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={data.serie}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="anoMes" />
                  <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
                  <Tooltip formatter={(value: number) => formatCurrency(value)} />
                  <Line
                    type="monotone"
                    dataKey="saldoProjetado"
                    name="Saldo projetado"
                    stroke="#2563eb"
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>
        </>
      )}
    </div>
  );
}
