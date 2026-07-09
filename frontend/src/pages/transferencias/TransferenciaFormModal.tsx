import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreateTransfer } from "@/hooks/useTransfers";
import { extractErrorMessage } from "@/lib/api";
import { todayIsoDate } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z
  .object({
    contaOrigemId: z.coerce.number({ invalid_type_error: "Selecione a conta de origem" }),
    contaDestinoId: z.coerce.number({ invalid_type_error: "Selecione a conta de destino" }),
    valor: z.coerce.number({ invalid_type_error: "Informe um valor" }).positive("O valor deve ser positivo"),
    data: z.string().min(1, "A data é obrigatória"),
    descricao: z.string().max(150).optional().or(z.literal("")),
  })
  .refine((values) => values.contaOrigemId !== values.contaDestinoId, {
    message: "A conta de origem e destino devem ser diferentes",
    path: ["contaDestinoId"],
  });

type FormValues = z.infer<typeof schema>;

interface TransferenciaFormModalProps {
  onClose: () => void;
}

export function TransferenciaFormModal({ onClose }: TransferenciaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const { data: accounts } = useBankAccounts();
  const createTransfer = useCreateTransfer();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      contaOrigemId: undefined,
      contaDestinoId: undefined,
      valor: 0,
      data: todayIsoDate(),
      descricao: "",
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      contaOrigemId: values.contaOrigemId,
      contaDestinoId: values.contaDestinoId,
      valor: values.valor,
      data: values.data,
      descricao: values.descricao || undefined,
    };

    try {
      await createTransfer.mutateAsync(payload);
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title="Nova transferência" isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <div className="grid grid-cols-2 gap-4">
          <Select
            label="Conta de origem"
            error={errors.contaOrigemId?.message as string | undefined}
            {...register("contaOrigemId")}
          >
            <option value="">Selecione</option>
            {accounts?.map((account) => (
              <option key={account.id} value={account.id}>
                {account.nome}
              </option>
            ))}
          </Select>
          <Select
            label="Conta de destino"
            error={errors.contaDestinoId?.message as string | undefined}
            {...register("contaDestinoId")}
          >
            <option value="">Selecione</option>
            {accounts?.map((account) => (
              <option key={account.id} value={account.id}>
                {account.nome}
              </option>
            ))}
          </Select>
        </div>
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
        <Input
          label="Descrição"
          placeholder="Opcional"
          error={errors.descricao?.message}
          {...register("descricao")}
        />
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            Transferir
          </Button>
        </div>
      </form>
    </Modal>
  );
}
