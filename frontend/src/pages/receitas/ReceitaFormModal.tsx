import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCategories } from "@/hooks/useCategories";
import { useCreateIncome, useUpdateIncome } from "@/hooks/useIncomes";
import { extractErrorMessage } from "@/lib/api";
import type { Income } from "@/types/income";
import { todayIsoDate } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  descricao: z.string().min(1, "A descrição é obrigatória").max(150),
  valor: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor deve ser positivo"),
  data: z.string().min(1, "A data é obrigatória"),
  categoriaId: z.coerce.number({ invalid_type_error: "Selecione uma categoria" }),
  contaBancariaId: z.coerce.number({ invalid_type_error: "Selecione uma conta" }),
  recorrente: z.boolean(),
  recebida: z.boolean(),
  observacoes: z.string().max(500).optional().or(z.literal("")),
});

type FormValues = z.infer<typeof schema>;

interface ReceitaFormModalProps {
  income: Income | null;
  onClose: () => void;
}

export function ReceitaFormModal({ income, onClose }: ReceitaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const { data: categories } = useCategories();
  const createIncome = useCreateIncome();
  const updateIncome = useUpdateIncome();
  const isEditing = income !== null;

  const incomeCategories = categories?.filter((c) => c.tipo === "RECEITA" || c.tipo === "AMBOS");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      descricao: income?.descricao ?? "",
      valor: income?.valor ?? 0,
      data: income?.data ?? todayIsoDate(),
      categoriaId: income?.categoriaId ?? undefined,
      contaBancariaId: income?.contaBancariaId ?? undefined,
      recorrente: income?.recorrente ?? false,
      recebida: income?.recebida ?? true,
      observacoes: income?.observacoes ?? "",
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      descricao: values.descricao,
      valor: values.valor,
      data: values.data,
      categoriaId: values.categoriaId,
      contaBancariaId: values.contaBancariaId,
      recorrente: values.recorrente,
      recebida: values.recebida,
      observacoes: values.observacoes || undefined,
    };

    try {
      if (isEditing) {
        await updateIncome.mutateAsync({ id: income.id, payload });
      } else {
        await createIncome.mutateAsync(payload);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar receita" : "Nova receita"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Descrição"
          placeholder="Ex: Salário"
          error={errors.descricao?.message}
          {...register("descricao")}
        />
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Valor"
            type="number"
            step="0.01"
            error={errors.valor?.message}
            {...register("valor")}
          />
          <Input label="Data" type="date" error={errors.data?.message} {...register("data")} />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Select
            label="Categoria"
            error={errors.categoriaId?.message as string | undefined}
            {...register("categoriaId")}
          >
            <option value="">Selecione</option>
            {incomeCategories?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.nome}
              </option>
            ))}
          </Select>
          <Select
            label="Conta"
            error={errors.contaBancariaId?.message as string | undefined}
            {...register("contaBancariaId")}
          >
            <option value="">Selecione</option>
            {accounts?.map((account) => (
              <option key={account.id} value={account.id}>
                {account.nome}
              </option>
            ))}
          </Select>
        </div>
        <Textarea
          label="Observações"
          placeholder="Opcional"
          error={errors.observacoes?.message}
          {...register("observacoes")}
        />
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("recorrente")} />
          Receita recorrente
        </label>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("recebida")} />
          Já recebida
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
