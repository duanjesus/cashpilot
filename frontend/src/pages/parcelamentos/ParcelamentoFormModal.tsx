import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreditCards } from "@/hooks/useCreditCards";
import { useCategories } from "@/hooks/useCategories";
import { useCreateParcelamento } from "@/hooks/useParcelamentos";
import { extractErrorMessage } from "@/lib/api";
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
    valorTotal: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor deve ser positivo"),
    numeroParcelas: z.coerce
      .number({ invalid_type_error: "Informe o número de parcelas" })
      .int("O número de parcelas deve ser inteiro")
      .min(2, "O parcelamento deve ter ao menos 2 parcelas"),
    dataPrimeiraParcela: z.string().min(1, "A data da primeira parcela é obrigatória"),
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

interface ParcelamentoFormModalProps {
  onClose: () => void;
}

export function ParcelamentoFormModal({ onClose }: ParcelamentoFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const { data: cards } = useCreditCards();
  const { data: categories } = useCategories();
  const createParcelamento = useCreateParcelamento();

  const expenseCategories = categories?.filter((c) => c.tipo === "DESPESA" || c.tipo === "AMBOS");

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      descricao: "",
      valorTotal: 0,
      numeroParcelas: 2,
      dataPrimeiraParcela: todayIsoDate(),
      categoriaId: undefined,
      metodoPagamento: "conta" as PaymentMethod,
      contaBancariaId: "",
      cartaoCreditoId: "",
      observacoes: "",
    },
  });

  const metodoPagamento = watch("metodoPagamento");

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      descricao: values.descricao,
      valorTotal: values.valorTotal,
      numeroParcelas: values.numeroParcelas,
      dataPrimeiraParcela: values.dataPrimeiraParcela,
      categoriaId: values.categoriaId,
      contaBancariaId: values.metodoPagamento === "conta" ? Number(values.contaBancariaId) : null,
      cartaoCreditoId: values.metodoPagamento === "cartao" ? Number(values.cartaoCreditoId) : null,
      observacoes: values.observacoes || undefined,
    };

    try {
      await createParcelamento.mutateAsync(payload);
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title="Novo parcelamento" isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Descrição"
          placeholder="Ex: Notebook em 10x"
          error={errors.descricao?.message}
          {...register("descricao")}
        />
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Valor total"
            type="number"
            step="0.01"
            error={errors.valorTotal?.message}
            {...register("valorTotal")}
          />
          <Input
            label="Número de parcelas"
            type="number"
            min={2}
            step="1"
            error={errors.numeroParcelas?.message}
            {...register("numeroParcelas")}
          />
        </div>
        <Input
          label="Data da primeira parcela"
          type="date"
          error={errors.dataPrimeiraParcela?.message}
          {...register("dataPrimeiraParcela")}
        />
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
            Cadastrar
          </Button>
        </div>
      </form>
    </Modal>
  );
}
