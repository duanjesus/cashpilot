import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useCalculateProjection, useLastSimulation } from "@/hooks/useProjection";
import { extractErrorMessage } from "@/lib/api";
import type { ProjectionResult } from "@/types/projection";
import { formatCurrency, formatDate } from "@/utils/format";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Spinner } from "@/components/ui/Spinner";

const schema = z.object({
  salario: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  despesasFixas: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  despesasVariaveis: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  investimentoMensal: z.union([z.literal(""), z.coerce.number().min(0)]).optional(),
  patrimonioAtual: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  valorAlvo: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor alvo deve ser positivo"),
  taxaRetornoMensal: z.coerce.number({ invalid_type_error: "Informe uma taxa" }).min(0),
});

type FormValues = z.infer<typeof schema>;

export function ProjectionPage() {
  const { data: lastSimulation, isLoading: isLastSimulationLoading } = useLastSimulation();
  const calculateProjection = useCalculateProjection();
  const [result, setResult] = useState<ProjectionResult | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      salario: 0,
      despesasFixas: 0,
      despesasVariaveis: 0,
      investimentoMensal: "",
      patrimonioAtual: 0,
      valorAlvo: 0,
      taxaRetornoMensal: 0,
    },
  });

  useEffect(() => {
    if (lastSimulation) {
      reset({
        salario: lastSimulation.salario,
        despesasFixas: lastSimulation.despesasFixas,
        despesasVariaveis: lastSimulation.despesasVariaveis,
        investimentoMensal: lastSimulation.investimentoMensal ?? "",
        patrimonioAtual: lastSimulation.patrimonioAtual,
        valorAlvo: lastSimulation.valorAlvo,
        taxaRetornoMensal: lastSimulation.taxaRetornoMensal,
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lastSimulation]);

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      salario: values.salario,
      despesasFixas: values.despesasFixas,
      despesasVariaveis: values.despesasVariaveis,
      investimentoMensal: values.investimentoMensal === "" ? undefined : Number(values.investimentoMensal),
      patrimonioAtual: values.patrimonioAtual,
      valorAlvo: values.valorAlvo,
      taxaRetornoMensal: values.taxaRetornoMensal,
    };

    try {
      const data = await calculateProjection.mutateAsync(payload);
      setResult(data);
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Projeção de patrimônio</h1>
        <p className="text-sm text-slate-500">
          Simule quando você atingirá um patrimônio alvo mantendo o ritmo atual de economia e investimento.
        </p>
      </div>

      {isLastSimulationLoading && (
        <div className="flex justify-center p-6">
          <Spinner />
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
        >
          <ErrorBanner message={serverError} />
          <Input
            label="Salário mensal"
            type="number"
            step="0.01"
            error={errors.salario?.message}
            {...register("salario")}
          />
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Despesas fixas"
              type="number"
              step="0.01"
              error={errors.despesasFixas?.message}
              {...register("despesasFixas")}
            />
            <Input
              label="Despesas variáveis"
              type="number"
              step="0.01"
              error={errors.despesasVariaveis?.message}
              {...register("despesasVariaveis")}
            />
          </div>
          <Input
            label="Investimento mensal (opcional)"
            type="number"
            step="0.01"
            error={errors.investimentoMensal?.message as string | undefined}
            {...register("investimentoMensal")}
          />
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Patrimônio atual"
              type="number"
              step="0.01"
              error={errors.patrimonioAtual?.message}
              {...register("patrimonioAtual")}
            />
            <Input
              label="Valor alvo"
              type="number"
              step="0.01"
              error={errors.valorAlvo?.message}
              {...register("valorAlvo")}
            />
          </div>
          <Input
            label="Taxa de retorno mensal (%)"
            type="number"
            step="0.01"
            error={errors.taxaRetornoMensal?.message}
            {...register("taxaRetornoMensal")}
          />
          <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
            Calcular projeção
          </Button>
        </form>

        <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <h2 className="mb-3 font-semibold text-slate-900">Resultado</h2>
          {!result && <p className="text-sm text-slate-500">Preencha o formulário e calcule para ver o resultado.</p>}
          {result && result.atingivel && (
            <div className="flex flex-col gap-2 text-sm text-slate-700">
              <p>
                Mantendo esse ritmo, você atingirá <strong>{formatCurrency(result.valorFinalProjetado)}</strong> em
                patrimônio em aproximadamente{" "}
                <strong>
                  {result.anos ?? 0} ano(s) e {result.mesesRestantes ?? 0} mês(es)
                </strong>
                {result.dataEstimada && <> (por volta de {formatDate(result.dataEstimada)})</>}.
              </p>
              <p className="text-xs text-slate-500">
                Aporte mensal considerado: {formatCurrency(result.aporteMensal)}.
              </p>
            </div>
          )}
          {result && !result.atingivel && (
            <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Com o aporte mensal atual de {formatCurrency(result.aporteMensal)}, a meta de{" "}
              {formatCurrency(result.valorAlvo)} não é atingível nesse ritmo. Aumente o investimento mensal ou
              reduza o valor alvo para recalcular.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
