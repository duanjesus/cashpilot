import { useMemo, useState } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { useCompararSimulacoes } from "@/hooks/useSimulacao";
import { extractErrorMessage } from "@/lib/api";
import type { SimulacaoCompararResponse } from "@/types/simulacao";
import { formatCurrency } from "@/utils/format";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const LINE_COLORS = ["#2563eb", "#f97316", "#22c55e"];

const cenarioSchema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(50),
  patrimonioInicial: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  aporteMensal: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  taxaRetornoMensal: z.coerce.number({ invalid_type_error: "Informe uma taxa" }),
});

const schema = z.object({
  horizonteMeses: z.coerce
    .number({ invalid_type_error: "Informe o horizonte" })
    .int("O horizonte deve ser um número inteiro de meses")
    .min(1, "O horizonte deve ser de ao menos 1 mês"),
  cenarios: z.array(cenarioSchema).min(2, "Adicione ao menos 2 cenários").max(3, "No máximo 3 cenários"),
});

type FormValues = z.infer<typeof schema>;

function defaultCenario(nome: string) {
  return { nome, patrimonioInicial: 0, aporteMensal: 0, taxaRetornoMensal: 0 };
}

export function SimulacaoPage() {
  const compararSimulacoes = useCompararSimulacoes();
  const [result, setResult] = useState<SimulacaoCompararResponse | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      horizonteMeses: 24,
      cenarios: [defaultCenario("Cenário A"), defaultCenario("Cenário B")],
    },
  });

  const { fields, append, remove } = useFieldArray({ control, name: "cenarios" });

  const cenariosArrayError = errors.cenarios as unknown as
    | { message?: string; root?: { message?: string } }
    | undefined;

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      const data = await compararSimulacoes.mutateAsync(values);
      setResult(data);
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  const chartData = useMemo(() => {
    if (!result) return [];
    return Array.from({ length: result.horizonteMeses + 1 }, (_, mes) => {
      const point: Record<string, number | string> = { mes };
      result.cenarios.forEach((cenario) => {
        point[cenario.nome] = cenario.pontos[mes];
      });
      return point;
    });
  }, [result]);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Simulação financeira</h1>
        <p className="text-sm text-slate-500">
          Compare até 3 cenários de patrimônio, aporte mensal e taxa de retorno ao longo do tempo.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <ErrorBanner message={cenariosArrayError?.root?.message ?? cenariosArrayError?.message ?? null} />

        <div className="max-w-xs">
          <Input
            label="Horizonte (meses)"
            type="number"
            step="1"
            min={1}
            error={errors.horizonteMeses?.message}
            {...register("horizonteMeses")}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
          {fields.map((field, index) => (
            <div key={field.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div className="mb-3 flex items-center justify-between">
                <h2 className="font-semibold text-slate-900">Cenário {index + 1}</h2>
                {fields.length > 2 && (
                  <Button type="button" variant="ghost" onClick={() => remove(index)}>
                    Remover
                  </Button>
                )}
              </div>
              <div className="flex flex-col gap-3">
                <Input
                  label="Nome"
                  error={errors.cenarios?.[index]?.nome?.message}
                  {...register(`cenarios.${index}.nome` as const)}
                />
                <Input
                  label="Patrimônio inicial"
                  type="number"
                  step="0.01"
                  error={errors.cenarios?.[index]?.patrimonioInicial?.message}
                  {...register(`cenarios.${index}.patrimonioInicial` as const)}
                />
                <Input
                  label="Aporte mensal"
                  type="number"
                  step="0.01"
                  error={errors.cenarios?.[index]?.aporteMensal?.message}
                  {...register(`cenarios.${index}.aporteMensal` as const)}
                />
                <Input
                  label="Taxa de retorno mensal (%)"
                  type="number"
                  step="0.01"
                  error={errors.cenarios?.[index]?.taxaRetornoMensal?.message}
                  {...register(`cenarios.${index}.taxaRetornoMensal` as const)}
                />
              </div>
            </div>
          ))}
        </div>

        <div className="flex items-center gap-3">
          {fields.length < 3 && (
            <Button
              type="button"
              variant="secondary"
              onClick={() => append(defaultCenario(`Cenário ${String.fromCharCode(65 + fields.length)}`))}
            >
              + Adicionar cenário
            </Button>
          )}
          <Button type="submit" isLoading={isSubmitting || compararSimulacoes.isPending}>
            Comparar
          </Button>
        </div>
      </form>

      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <h2 className="mb-1 font-semibold text-slate-900">Resultado</h2>
        <p className="mb-3 text-xs text-slate-500">
          Evolução patrimonial projetada para cada cenário ao longo do horizonte informado.
        </p>
        {!result && <p className="text-sm text-slate-500">Preencha os cenários e clique em Comparar para ver o resultado.</p>}
        {result && (
          <ResponsiveContainer width="100%" height={320}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="mes" tickFormatter={(v: number) => `Mês ${v}`} />
              <YAxis tickFormatter={(v: number) => formatCurrency(v)} width={90} />
              <Tooltip labelFormatter={(v) => `Mês ${v}`} formatter={(value: number) => formatCurrency(value)} />
              <Legend />
              {result.cenarios.map((cenario, index) => (
                <Line
                  key={cenario.nome}
                  type="monotone"
                  dataKey={cenario.nome}
                  name={cenario.nome}
                  stroke={LINE_COLORS[index % LINE_COLORS.length]}
                  dot={false}
                />
              ))}
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
