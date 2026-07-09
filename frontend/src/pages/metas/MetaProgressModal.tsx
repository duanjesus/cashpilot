import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useUpdateGoalProgress } from "@/hooks/useGoals";
import { extractErrorMessage } from "@/lib/api";
import type { Goal } from "@/types/goal";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  valorAtual: z.coerce.number({ invalid_type_error: "Informe um valor" }).min(0),
});

type FormValues = z.infer<typeof schema>;

interface MetaProgressModalProps {
  goal: Goal;
  onClose: () => void;
}

export function MetaProgressModal({ goal, onClose }: MetaProgressModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const updateProgress = useUpdateGoalProgress();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { valorAtual: goal.valorAtual },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      await updateProgress.mutateAsync({ id: goal.id, payload: values });
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={`Atualizar progresso — ${goal.nome}`} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Valor atual"
          type="number"
          step="0.01"
          error={errors.valorAtual?.message}
          {...register("valorAtual")}
        />
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            Salvar
          </Button>
        </div>
      </form>
    </Modal>
  );
}
