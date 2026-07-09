import { useState } from "react";

import { useCategories, useDeleteCategory } from "@/hooks/useCategories";
import { extractErrorMessage } from "@/lib/api";
import type { Category } from "@/types/category";
import { CATEGORY_TYPE_LABELS } from "@/types/category";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { CategoriaFormModal } from "@/pages/categorias/CategoriaFormModal";

export function CategoriasListPage() {
  const { data, isLoading, isError } = useCategories();
  const deleteCategory = useDeleteCategory();

  const [modalState, setModalState] = useState<{ open: boolean; category: Category | null }>({
    open: false,
    category: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(category: Category) {
    if (category.isSystem) return;
    if (!window.confirm(`Excluir a categoria "${category.nome}"?`)) return;
    setActionError(null);
    try {
      await deleteCategory.mutateAsync(category.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Categorias</h1>
          <p className="text-sm text-slate-500">Categorias usadas para classificar receitas e despesas.</p>
        </div>
        <Button onClick={() => setModalState({ open: true, category: null })}>+ Nova categoria</Button>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as categorias." />}
        {!isLoading && data?.length === 0 && <EmptyState message="Nenhuma categoria cadastrada ainda." />}
        {!isLoading && data && data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Nome</th>
                  <th className="px-4 py-3 font-medium">Tipo</th>
                  <th className="px-4 py-3 font-medium">Cor</th>
                  <th className="px-4 py-3 font-medium">Origem</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((category) => (
                  <tr key={category.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {category.nome}
                      {category.isInvestment && (
                        <Badge tone="blue" >
                          Investimento
                        </Badge>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{CATEGORY_TYPE_LABELS[category.tipo]}</td>
                    <td className="px-4 py-3">
                      {category.cor && (
                        <span
                          className="inline-block h-4 w-4 rounded-full border border-slate-200"
                          style={{ backgroundColor: category.cor }}
                        />
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={category.isSystem ? "slate" : "green"}>
                        {category.isSystem ? "Sistema" : "Personalizada"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="secondary"
                          disabled={category.isSystem}
                          onClick={() => setModalState({ open: true, category })}
                        >
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          disabled={category.isSystem}
                          onClick={() => handleDelete(category)}
                          isLoading={deleteCategory.isPending}
                        >
                          Excluir
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modalState.open && (
        <CategoriaFormModal
          category={modalState.category}
          onClose={() => setModalState({ open: false, category: null })}
        />
      )}
    </div>
  );
}
