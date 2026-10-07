import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import type { SaldoHistoricoPonto, SaldoOrigem } from "@/types/saldoHistorico";
import { formatCurrency, formatDate } from "@/utils/format";

const LINE_COLOR = "#2563eb";
const DIVERGENT_COLOR = "#f59e0b";

const ORIGEM_LABELS: Record<SaldoOrigem, string> = {
  CAPTURADO: "Registrado no fechamento do dia",
  RECONSTRUIDO: "Reconstruído a partir dos lançamentos",
};

interface DotProps {
  cx?: number;
  cy?: number;
  index?: number;
  payload?: SaldoHistoricoPonto;
}

function renderDot({ cx, cy, index, payload }: DotProps) {
  if (!payload?.divergente || cx == null || cy == null) return <g key={index} />;
  return <circle key={index} cx={cx} cy={cy} r={4} fill={DIVERGENT_COLOR} stroke="#ffffff" strokeWidth={2} />;
}

function PontoTooltip({ active, payload }: { active?: boolean; payload?: { payload: SaldoHistoricoPonto }[] }) {
  const ponto = payload?.[0]?.payload;
  if (!active || !ponto) return null;
  return (
    <div className="max-w-64 rounded-md border border-slate-200 bg-white p-3 text-xs shadow-md">
      <p className="font-medium text-slate-900">{formatDate(ponto.data)}</p>
      <p className="mt-1 text-sm font-semibold text-slate-900">{formatCurrency(ponto.saldo)}</p>
      {ponto.divergente && ponto.saldoRegistrado !== undefined ? (
        <p className="mt-1 text-slate-600">
          <span
            className="mr-1 inline-block h-2 w-2 rounded-full align-middle"
            style={{ backgroundColor: DIVERGENT_COLOR }}
          />
          No fechamento do dia o saldo registrado era {formatCurrency(ponto.saldoRegistrado)}. Lançamentos
          alterados depois mudaram este dia.
        </p>
      ) : (
        ponto.origem && <p className="mt-1 text-slate-500">{ORIGEM_LABELS[ponto.origem]}</p>
      )}
    </div>
  );
}

export function SaldoHistoricoChart({ data, height = 240 }: { data: SaldoHistoricoPonto[]; height?: number }) {
  const temDivergencia = data.some((ponto) => ponto.divergente);

  return (
    <div>
      <ResponsiveContainer width="100%" height={height}>
        <LineChart data={data} margin={{ top: 8, right: 24, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="data" tickFormatter={(v: string) => formatDate(v)} minTickGap={32} />
          <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={100} />
          <Tooltip content={<PontoTooltip />} />
          <Line
            type="linear"
            dataKey="saldo"
            name="Saldo"
            stroke={LINE_COLOR}
            strokeWidth={2}
            dot={renderDot}
            activeDot={{ r: 4 }}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
      {temDivergencia && (
        <p className="mt-2 text-xs text-slate-500">
          <span
            className="mr-1 inline-block h-2 w-2 rounded-full align-middle"
            style={{ backgroundColor: DIVERGENT_COLOR }}
          />
          Dias em que o saldo mudou depois do fechamento, por lançamentos incluídos ou alterados mais tarde.
        </p>
      )}
    </div>
  );
}
