import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreditCards } from "@/hooks/useCreditCards";
import { useCategories } from "@/hooks/useCategories";
import { useCreateExpense, useUpdateExpense } from "@/hooks/useExpenses";
import { extractErrorMessage } from "@/lib/api";
import type { Expense } from "@/types/expense";
import { todayIsoDate } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

type PaymentMethod = "conta" | "cartao";

const schema = z
  .object({
    descricao: z.string().min(1, "A descrição é obrigatória").max(150),
    valor: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor deve ser positivo"),
    data: z.string().min(1, "A data é obrigatória"),
    categoriaId: z.coerce.number({ invalid_type_error: "Selecione uma categoria" }),
    metodoPagamento: z.enum(["conta", "cartao"]),
    contaBancariaId: z.union([z.literal(""), z.coerce.number()]).optional(),
    cartaoCreditoId: z.union([z.literal(""), z.coerce.number()]).optional(),
    observacoes: z.string().max(500).optional().or(z.literal("")),
  })
  .superRefine((values, ctx) => {
    if (values.metodoPagamento === "conta" && !values.contaBancariaId) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["contaBancariaId"],
        message: "Selecione uma conta bancária",
      });
    }
    if (values.metodoPagamento === "cartao" && !values.cartaoCreditoId) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["cartaoCreditoId"],
        message: "Selecione um cartão de crédito",
      });
    }
  });

type FormValues = z.infer<typeof schema>;

interface DespesaFormModalProps {
  expense: Expense | null;
  onClose: () => void;
}

export function DespesaFormModal({ expense, onClose }: DespesaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const { data: cards } = useCreditCards();
  const { data: categories } = useCategories();
  const createExpense = useCreateExpense();
  const updateExpense = useUpdateExpense();
  const isEditing = expense !== null;

  const expenseCategories = categories?.filter((c) => c.tipo === "DESPESA" || c.tipo === "AMBOS");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      descricao: expense?.descricao ?? "",
      valor: expense?.valor ?? 0,
      data: expense?.data ?? todayIsoDate(),
      categoriaId: expense?.categoriaId ?? undefined,
      metodoPagamento: (expense?.cartaoCreditoId ? "cartao" : "conta") as PaymentMethod,
      contaBancariaId: expense?.contaBancariaId ?? "",
      cartaoCreditoId: expense?.cartaoCreditoId ?? "",
      observacoes: expense?.observacoes ?? "",
    },
  });

  const metodoPagamento = watch("metodoPagamento");

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      descricao: values.descricao,
      valor: values.valor,
      data: values.data,
      categoriaId: values.categoriaId,
      contaBancariaId: values.metodoPagamento === "conta" ? Number(values.contaBancariaId) : null,
      cartaoCreditoId: values.metodoPagamento === "cartao" ? Number(values.cartaoCreditoId) : null,
      observacoes: values.observacoes || undefined,
    };

    try {
      if (isEditing) {
        await updateExpense.mutateAsync({ id: expense.id, payload });
      } else {
        await createExpense.mutateAsync(payload);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar despesa" : "Nova despesa"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Descrição"
          placeholder="Ex: Supermercado"
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
        <Select
          label="Categoria"
          error={errors.categoriaId?.message as string | undefined}
          {...register("categoriaId")}
        >
          <option value="">Selecione</option>
          {expenseCategories?.map((category) => (
            <option key={category.id} value={category.id}>
              {category.nome}
            </option>
          ))}
        </Select>
        <Select label="Forma de pagamento" {...register("metodoPagamento")}>
          <option value="conta">Conta bancária</option>
          <option value="cartao">Cartão de crédito</option>
        </Select>
        {metodoPagamento === "conta" ? (
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
        ) : (
          <Select
            label="Cartão"
            error={errors.cartaoCreditoId?.message as string | undefined}
            {...register("cartaoCreditoId")}
          >
            <option value="">Selecione</option>
            {cards?.map((card) => (
              <option key={card.id} value={card.id}>
                {card.nome}
              </option>
            ))}
          </Select>
        )}
        <Textarea
          label="Observações"
          placeholder="Opcional"
          error={errors.observacoes?.message}
          {...register("observacoes")}
        />
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
