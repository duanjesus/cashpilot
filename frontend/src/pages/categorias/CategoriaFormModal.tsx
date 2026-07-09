import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import { useCreateCategory, useUpdateCategory } from "@/hooks/useCategories";
import { extractErrorMessage } from "@/lib/api";
import type { Category } from "@/types/category";
import { CATEGORY_TYPE_LABELS } from "@/types/category";
import { Modal } from "@/components/ui/Modal";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { ErrorBanner } from "@/components/ui/ErrorBanner";

const schema = z.object({
  nome: z.string().min(1, "O nome é obrigatório").max(100),
  tipo: z.enum(["RECEITA", "DESPESA", "AMBOS"]),
  cor: z.string().max(20).optional().or(z.literal("")),
});

type FormValues = z.infer<typeof schema>;

interface CategoriaFormModalProps {
  category: Category | null;
  onClose: () => void;
}

export function CategoriaFormModal({ category, onClose }: CategoriaFormModalProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const createCategory = useCreateCategory();
  const updateCategory = useUpdateCategory();
  const isEditing = category !== null;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      nome: category?.nome ?? "",
      tipo: category?.tipo ?? "DESPESA",
      cor: category?.cor ?? "#3b82f6",
    },
  });

  async function onSubmit(values: FormValues) {
    setServerError(null);
    const payload = {
      nome: values.nome,
      tipo: values.tipo,
      cor: values.cor || undefined,
    };

    try {
      if (isEditing) {
        await updateCategory.mutateAsync({ id: category.id, payload });
      } else {
        await createCategory.mutateAsync(payload);
      }
      onClose();
    } catch (error) {
      setServerError(extractErrorMessage(error));
    }
  }

  return (
    <Modal title={isEditing ? "Editar categoria" : "Nova categoria"} isOpen onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <ErrorBanner message={serverError} />
        <Input
          label="Nome"
          placeholder="Ex: Alimentação"
          error={errors.nome?.message}
          {...register("nome")}
        />
        <Select label="Tipo" error={errors.tipo?.message} {...register("tipo")}>
          {Object.entries(CATEGORY_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
        <Input
          label="Cor"
          type="color"
          className="h-10 w-20 p-1"
          error={errors.cor?.message}
          {...register("cor")}
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
