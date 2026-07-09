import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreditCards } from "@/hooks/useCreditCards";
import { useCategories } from "@/hooks/useCategories";
import { useCreateSubscription, useUpdateSubscription } from "@/hooks/useSubscriptions";
import { extractErrorMessage } from "@/lib/api";
import type { Subscription } from "@/types/subscription";
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
    diaCobranca: z.coerce
      .number({ invalid_type_error: "Informe o dia de cobrança" })
      .int("O dia deve ser inteiro")
      .min(1, "O dia deve estar entre 1 e 31")
      .max(31, "O dia deve estar entre 1 e 31"),
    dataInicio: z.string().min(1, "A data de início é obrigatória"),
    dataFim: z.union([z.literal(""), z.string()]).optional(),
    ativa: z.boolean(),
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

interface AssinaturaFormModalProps {
  subscription: Subscription | null;
  onClose: () => void;
}

export function AssinaturaFormModal({ subscription, onClose }: AssinaturaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const { data: cards } = useCreditCards();
  const { data: categories } = useCategories();
  const createSubscription = useCreateSubscription();
  const updateSubscription = useUpdateSubscription();
  const isEditing = subscription !== null;

  const expenseCategories = categories?.filter((c) => c.tipo === "DESPESA" || c.tipo === "AMBOS");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      descricao: subscription?.descricao ?? "",
      valor: subscription?.valor ?? 0,
      diaCobranca: subscription?.diaCobranca ?? 1,
      dataInicio: subscription?.dataInicio ?? todayIsoDate(),
      dataFim: subscription?.dataFim ?? "",
      ativa: subscription?.ativa ?? true,
      categoriaId: subscription?.categoriaId ?? undefined,
      metodoPagamento: (subscription?.cartaoCreditoId ? "cartao" : "conta") as PaymentMethod,
      contaBancariaId: subscription?.contaBancariaId ?? "",
      cartaoCreditoId: subscription?.cartaoCreditoId ?? "",
      observacoes: subscription?.observacoes ?? "",
    },
  });

  const metodoPagamento = watch("metodoPagamento");

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      descricao: values.descricao,
      valor: values.valor,
      diaCobranca: values.diaCobranca,
      dataInicio: values.dataInicio,
      dataFim: values.dataFim || null,
      ativa: values.ativa,
      categoriaId: values.categoriaId,
      contaBancariaId: values.metodoPagamento === "conta" ? Number(values.contaBancariaId) : null,
      cartaoCreditoId: values.metodoPagamento === "cartao" ? Number(values.cartaoCreditoId) : null,
      observacoes: values.observacoes || undefined,
    };

    try {
      if (isEditing) {
        await updateSubscription.mutateAsync({ id: subscription.id, payload });
      } else {
        await createSubscription.mutateAsync(payload);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar assinatura" : "Nova assinatura"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Descrição"
          placeholder="Ex: Streaming de vídeo"
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
          <Input
            label="Dia de cobrança"
            type="number"
            min={1}
            max={31}
            step="1"
            error={errors.diaCobranca?.message}
            {...register("diaCobranca")}
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Input label="Data de início" type="date" error={errors.dataInicio?.message} {...register("dataInicio")} />
          <Input
            label="Data de término"
            type="date"
            placeholder="Opcional"
            error={errors.dataFim?.message}
            {...register("dataFim")}
          />
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
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("ativa")} />
          Assinatura ativa
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
