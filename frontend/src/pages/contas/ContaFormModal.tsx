import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useCreateBankAccount, useUpdateBankAccount } from "@/hooks/useBankAccounts";
import { extractErrorMessage } from "@/lib/api";
import type { BankAccount } from "@/types/bankAccount";
import { BANK_ACCOUNT_TYPE_LABELS } from "@/types/bankAccount";
import { todayIsoDate } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(100),
  instituicao: z.string().min(1, "A instituição é obrigatória").max(100),
  tipo: z.enum(["CORRENTE", "POUPANCA", "CARTEIRA", "OUTRA"]),
  saldoInicial: z.coerce.number({ invalid_type_error: "Informe um valor" }),
  dataSaldoInicial: z.string().min(1, "A data é obrigatória"),
  ativa: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

interface ContaFormModalProps {
  account: BankAccount | null;
  onClose: () => void;
}

export function ContaFormModal({ account, onClose }: ContaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const createAccount = useCreateBankAccount();
  const updateAccount = useUpdateBankAccount();
  const isEditing = account !== null;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: account?.nome ?? "",
      instituicao: account?.instituicao ?? "",
      tipo: account?.tipo ?? "CORRENTE",
      saldoInicial: account?.saldoInicial ?? 0,
      dataSaldoInicial: account?.dataSaldoInicial ?? todayIsoDate(),
      ativa: account?.ativa ?? true,
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      if (isEditing) {
        await updateAccount.mutateAsync({ id: account.id, payload: values });
      } else {
        await createAccount.mutateAsync(values);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar conta" : "Nova conta"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Nome"
          placeholder="Ex: Conta Principal"
          error={errors.nome?.message}
          {...register("nome")}
        />
        <Input
          label="Instituição"
          placeholder="Ex: Banco do Brasil"
          error={errors.instituicao?.message}
          {...register("instituicao")}
        />
        <Select label="Tipo" error={errors.tipo?.message} {...register("tipo")}>
          {Object.entries(BANK_ACCOUNT_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Saldo inicial"
            type="number"
            step="0.01"
            error={errors.saldoInicial?.message}
            {...register("saldoInicial")}
          />
          <Input
            label="Data do saldo inicial"
            type="date"
            error={errors.dataSaldoInicial?.message}
            {...register("dataSaldoInicial")}
          />
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" className="h-4 w-4 rounded border-slate-300" {...register("ativa")} />
          Conta ativa
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
