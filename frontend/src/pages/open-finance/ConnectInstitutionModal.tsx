import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useConnectInstitution } from "@/hooks/useOpenFinance";
import { extractErrorMessage } from "@/lib/api";
import type { OpenFinanceInstitution } from "@/types/openFinance";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  tipoConta: z.enum(["CONTA", "CARTAO"]),
  apelido: z.string().min(1, "O apelido é obrigatório").max(150),
});

type FormValues = z.infer<typeof schema>;

interface ConnectInstitutionModalProps {
  institution: OpenFinanceInstitution;
  onClose: () => void;
}

export function ConnectInstitutionModal({ institution, onClose }: ConnectInstitutionModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const connectInstitution = useConnectInstitution();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { tipoConta: "CONTA", apelido: "" },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      await connectInstitution.mutateAsync({ instituicaoNome: institution.nome, ...values });
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={`Conectar com ${institution.nome}`} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Select label="Tipo" error={errors.tipoConta?.message} {...register("tipoConta")}>
          <option value="CONTA">Conta</option>
          <option value="CARTAO">Cartão</option>
        </Select>
        <Input
          label="Apelido"
          placeholder="Ex: Conta corrente principal"
          error={errors.apelido?.message}
          {...register("apelido")}
        />
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            Conectar
          </Button>
        </div>
      </form>
    </Modal>
  );
}
