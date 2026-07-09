import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreateCreditCard, useUpdateCreditCard } from "@/hooks/useCreditCards";
import { extractErrorMessage } from "@/lib/api";
import type { CreditCard } from "@/types/creditCard";
import { CARD_BRAND_LABELS } from "@/types/creditCard";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(100),
  bandeira: z.enum(["VISA", "MASTERCARD", "ELO", "AMEX", "OUTRA"]),
  limite: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O limite deve ser positivo"),
  diaFechamento: z.coerce.number().int().min(1).max(31),
  diaVencimento: z.coerce.number().int().min(1).max(31),
  contaVinculadaId: z.union([z.literal(""), z.coerce.number()]).optional(),
  ativo: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

interface CartaoFormModalProps {
  card: CreditCard | null;
  onClose: () => void;
}

export function CartaoFormModal({ card, onClose }: CartaoFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const createCard = useCreateCreditCard();
  const updateCard = useUpdateCreditCard();
  const isEditing = card !== null;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: card?.nome ?? "",
      bandeira: card?.bandeira ?? "VISA",
      limite: card?.limite ?? 0,
      diaFechamento: card?.diaFechamento ?? 1,
      diaVencimento: card?.diaVencimento ?? 10,
      contaVinculadaId: card?.contaVinculadaId ?? "",
      ativo: card?.ativo ?? true,
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      nome: values.nome,
      bandeira: values.bandeira,
      limite: values.limite,
      diaFechamento: values.diaFechamento,
      diaVencimento: values.diaVencimento,
      contaVinculadaId: values.contaVinculadaId === "" ? undefined : Number(values.contaVinculadaId),
      ativo: values.ativo,
    };

    try {
      if (isEditing) {
        await updateCard.mutateAsync({ id: card.id, payload });
      } else {
        await createCard.mutateAsync(payload);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar cartão" : "Novo cartão"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Nome"
          placeholder="Ex: Cartão Nubank"
          error={errors.nome?.message}
          {...register("nome")}
        />
        <div className="grid grid-cols-2 gap-4">
          <Select label="Bandeira" error={errors.bandeira?.message} {...register("bandeira")}>
            {Object.entries(CARD_BRAND_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
          <Input
            label="Limite"
            type="number"
            step="0.01"
            error={errors.limite?.message}
            {...register("limite")}
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Dia de fechamento"
            type="number"
            min={1}
            max={31}
            error={errors.diaFechamento?.message}
            {...register("diaFechamento")}
          />
          <Input
            label="Dia de vencimento"
            type="number"
            min={1}
            max={31}
            error={errors.diaVencimento?.message}
            {...register("diaVencimento")}
          />
        </div>
        <Select
          label="Conta vinculada (opcional)"
          error={errors.contaVinculadaId?.message as string | undefined}
          {...register("contaVinculadaId")}
        >
          <option value="">Nenhuma</option>
          {accounts?.map((account) => (
            <option key={account.id} value={account.id}>
              {account.nome}
            </option>
          ))}
        </Select>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("ativo")} />
          Cartão ativo
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
