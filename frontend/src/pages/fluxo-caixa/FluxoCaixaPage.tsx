import { useState } from "react";
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { useFluxoCaixa } from "@/hooks/useFluxoCaixa";
import { formatCurrency, formatDate } from "@/utils/format";
import type { TipoLancamentoFluxoCaixa } from "@/types/fluxoCaixa";
import { Badge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Select";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const TIPO_LABELS: Record<TipoLancamentoFluxoCaixa, string> = {
  DESPESA_PENDENTE: "Despesa pendente",
  RECEITA_PENDENTE: "Receita pendente",
  ASSINATURA_PROJETADA: "Assinatura projetada",
};

const TIPO_TONES: Record<TipoLancamentoFluxoCaixa, "green" | "red" | "amber"> = {
  DESPESA_PENDENTE: "red",
  RECEITA_PENDENTE: "green",
  ASSINATURA_PROJETADA: "amber",
};

function isEntrada(tipo: TipoLancamentoFluxoCaixa) {
  return tipo === "RECEITA_PENDENTE";
}

export function FluxoCaixaPage() {
  const [dias, setDias] = useState(30);
  const { data, isLoading, isError } = useFluxoCaixa(dias);

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Fluxo de caixa</h1>
          <p className="text-sm text-slate-500">
            Projeção do saldo futuro considerando despesas, receitas e assinaturas pendentes.
          </p>
        </div>
        <Select
          label="Período"
          value={String(dias)}
          onChange={(e) => setDias(Number(e.target.value))}
        >
          <option value="15">15 dias</option>
          <option value="30">30 dias</option>
          <option value="60">60 dias</option>
          <option value="90">90 dias</option>
        </Select>
      </div>

      {isLoading && (
        <div className="flex justify-center p-10">
          <Spinner />
        </div>
      )}
      {isError && <ErrorBanner message="Não foi possível carregar o fluxo de caixa." />}

      {!isLoading && data && (
        <>
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div className="mb-1 flex items-center justify-between">
              <h2 className="font-semibold text-slate-900">Saldo projetado</h2>
              <span className="text-sm text-slate-500">Saldo inicial: {formatCurrency(data.saldoInicial)}</span>
            </div>
            {data.serie.length === 0 ? (
              <EmptyState message="Sem dados suficientes para projetar o fluxo de caixa." />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={data.serie}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="data" tickFormatter={(v: string) => formatDate(v)} />
                  <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
                  <Tooltip
                    labelFormatter={(v) => formatDate(String(v))}
                    formatter={(value: number) => formatCurrency(value)}
                  />
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

          <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-200 px-4 py-3">
              <h2 className="font-semibold text-slate-900">Detalhamento</h2>
            </div>
            {data.detalhamento.length === 0 ? (
              <EmptyState message="Nenhum lançamento previsto para o período." />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">Data</th>
                      <th className="px-4 py-3 font-medium">Descrição</th>
                      <th className="px-4 py-3 font-medium">Origem</th>
                      <th className="px-4 py-3 font-medium">Tipo</th>
                      <th className="px-4 py-3 font-medium">Valor</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {data.detalhamento.map((item, index) => (
                      <tr key={`${item.data}-${item.descricao}-${index}`}>
                        <td className="px-4 py-3 text-slate-600">{formatDate(item.data)}</td>
                        <td className="px-4 py-3 font-medium text-slate-900">{item.descricao}</td>
                        <td className="px-4 py-3 text-slate-600">{item.origemNome}</td>
                        <td className="px-4 py-3">
                          <Badge tone={TIPO_TONES[item.tipo]}>{TIPO_LABELS[item.tipo]}</Badge>
                        </td>
                        <td
                          className={`px-4 py-3 font-medium ${
                            isEntrada(item.tipo) ? "text-green-700" : "text-red-700"
                          }`}
                        >
                          {isEntrada(item.tipo) ? "+" : "-"}
                          {formatCurrency(item.valor)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
