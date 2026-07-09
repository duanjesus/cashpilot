import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useCreateGoal, useUpdateGoal } from "@/hooks/useGoals";
import { extractErrorMessage } from "@/lib/api";
import type { Goal } from "@/types/goal";
import { todayIsoDate } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(150),
  valorAlvo: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor alvo deve ser positivo"),
  dataAlvo: z.string().min(1, "A data alvo é obrigatória"),
  valorAtual: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
  ativa: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

interface MetaFormModalProps {
  goal: Goal | null;
  onClose: () => void;
}

export function MetaFormModal({ goal, onClose }: MetaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const createGoal = useCreateGoal();
  const updateGoal = useUpdateGoal();
  const isEditing = goal !== null;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: goal?.nome ?? "",
      valorAlvo: goal?.valorAlvo ?? 0,
      dataAlvo: goal?.dataAlvo ?? todayIsoDate(),
      valorAtual: goal?.valorAtual ?? 0,
      ativa: goal?.ativa ?? true,
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      if (isEditing) {
        await updateGoal.mutateAsync({ id: goal.id, payload: values });
      } else {
        await createGoal.mutateAsync(values);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar meta" : "Nova meta"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Nome"
          placeholder="Ex: Reserva de emergência"
          error={errors.nome?.message}
          {...register("nome")}
        />
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Valor alvo"
            type="number"
            step="0.01"
            error={errors.valorAlvo?.message}
            {...register("valorAlvo")}
          />
          <Input label="Data alvo" type="date" error={errors.dataAlvo?.message} {...register("dataAlvo")} />
        </div>
        <Input
          label="Valor atual"
          type="number"
          step="0.01"
          error={errors.valorAtual?.message}
          {...register("valorAtual")}
        />
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("ativa")} />
          Meta ativa
        </label>
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            {isEditing ? "Salvar" : "Cadastrar"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
