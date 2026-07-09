import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useCreateFamilyGroup } from "@/hooks/useFamilyGroup";
import { extractErrorMessage } from "@/lib/api";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(150),
});

type FormValues = z.infer<typeof schema>;

interface CreateFamilyGroupModalProps {
  onClose: () => void;
}

export function CreateFamilyGroupModal({ onClose }: CreateFamilyGroupModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const createGroup = useCreateFamilyGroup();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { nome: "" },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    try {
      await createGroup.mutateAsync(values);
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title="Criar grupo familiar" isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Nome do grupo"
          placeholder="Ex: Família Silva"
          error={errors.nome?.message}
          {...register("nome")}
        />
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" isLoading={isSubmitting}>
            Criar grupo
          </Button>
        </div>
      </form>
    </Modal>
  );
}
