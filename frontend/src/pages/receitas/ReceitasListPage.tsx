import { useState } from "react";

import { useCategories } from "@/hooks/useCategories";
import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useDeleteIncome, useIncomes } from "@/hooks/useIncomes";
import { usePaginationState } from "@/hooks/usePaginationState";
import { extractErrorMessage } from "@/lib/api";
import type { Income } from "@/types/income";
import { formatCurrency, formatDate } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Select";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";
import { ReceitaFormModal } from "@/pages/receitas/ReceitaFormModal";

export function ReceitasListPage() {
  const { page, size, setPage } = usePaginationState();
  const [categoriaId, setCategoriaId] = useState<string>("");
  const [contaId, setContaId] = useState<string>("");

  const { data: categories } = useCategories();
  const { data: accounts } = useBankAccounts();
  const { data, isLoading, isError } = useIncomes({
    page,
    size,
    categoriaId: categoriaId ? Number(categoriaId) : undefined,
    contaId: contaId ? Number(contaId) : undefined,
  });
  const deleteIncome = useDeleteIncome();

  const [modalState, setModalState] = useState<{ open: boolean; income: Income | null }>({
    open: false,
    income: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(income: Income) {
    if (!window.confirm(`Excluir a receita "${income.descricao}"?`)) return;
    setActionError(null);
    try {
      await deleteIncome.mutateAsync(income.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Receitas</h1>
          <p className="text-sm text-slate-500">Entradas de dinheiro registradas nas suas contas.</p>
        </div>
        <Button onClick={() => setModalState({ open: true, income: null })}>+ Nova receita</Button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Select
          label="Filtrar por categoria"
          value={categoriaId}
          onChange={(e) => {
            setCategoriaId(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Todas</option>
          {categories?.map((category) => (
            <option key={category.id} value={category.id}>
              {category.nome}
            </option>
          ))}
        </Select>
        <Select
          label="Filtrar por conta"
          value={contaId}
          onChange={(e) => {
            setContaId(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Todas</option>
          {accounts?.map((account) => (
            <option key={account.id} value={account.id}>
              {account.nome}
            </option>
          ))}
        </Select>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as receitas." />}
        {!isLoading && data?.content.length === 0 && <EmptyState message="Nenhuma receita registrada ainda." />}
        {!isLoading && data && data.content.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Descrição</th>
                  <th className="px-4 py-3 font-medium">Categoria</th>
                  <th className="px-4 py-3 font-medium">Conta</th>
                  <th className="px-4 py-3 font-medium">Data</th>
                  <th className="px-4 py-3 font-medium">Valor</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((income) => (
                  <tr key={income.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {income.descricao}
                      {income.recorrente && (
                        <Badge tone="blue">Recorrente</Badge>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{income.categoriaNome}</td>
                    <td className="px-4 py-3 text-slate-600">{income.contaBancariaNome}</td>
                    <td className="px-4 py-3 text-slate-600">{formatDate(income.data)}</td>
                    <td className="px-4 py-3 font-medium text-green-700">{formatCurrency(income.valor)}</td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button variant="secondary" onClick={() => setModalState({ open: true, income })}>
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(income)}
                          isLoading={deleteIncome.isPending}
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
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      {modalState.open && (
        <ReceitaFormModal income={modalState.income} onClose={() => setModalState({ open: false, income: null })} />
      )}
    </div>
  );
}
