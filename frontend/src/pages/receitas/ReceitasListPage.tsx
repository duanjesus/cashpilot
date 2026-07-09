import { useState } from "react";

import { useCategories } from "@/hooks/useCategories";
import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useDeleteIncome, useIncomes, useMarkIncomeReceived } from "@/hooks/useIncomes";
import { useMyFamilyGroup } from "@/hooks/useFamilyGroup";
import { usePaginationState } from "@/hooks/usePaginationState";
import { api, extractErrorMessage } from "@/lib/api";
import type { Income, IncomeFilters } from "@/types/income";
import { formatCurrency, formatDate, todayIsoDate } from "@/utils/format";
import { downloadBlob } from "@/utils/download";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Select";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";
import { ReceitaFormModal } from "@/pages/receitas/ReceitaFormModal";

interface ReceitasListPageProps {
  initialFilters?: Partial<IncomeFilters>;
  title?: string;
  description?: string;
}

export function ReceitasListPage({ initialFilters, title, description }: ReceitasListPageProps) {
  const { page, size, setPage } = usePaginationState();
  const [categoriaId, setCategoriaId] = useState<string>("");
  const [contaId, setContaId] = useState<string>("");
  const [recebida, setRecebida] = useState<string>(
    initialFilters?.recebida === undefined ? "" : String(initialFilters.recebida),
  );

  const { data: categories } = useCategories();
  const { data: accounts } = useBankAccounts();
  const { data: familyGroup } = useMyFamilyGroup();
  const isViewer = familyGroup?.papelDoUsuarioAtual === "VIEWER";
  const { data, isLoading, isError } = useIncomes({
    page,
    size,
    categoriaId: categoriaId ? Number(categoriaId) : undefined,
    contaId: contaId ? Number(contaId) : undefined,
    recebida: recebida ? recebida === "true" : undefined,
  });
  const deleteIncome = useDeleteIncome();
  const markReceived = useMarkIncomeReceived();

  const [modalState, setModalState] = useState<{ open: boolean; income: Income | null }>({
    open: false,
    income: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);

  async function handleExport(formato: "xlsx" | "pdf") {
    setActionError(null);
    setIsExporting(true);
    try {
      const response = await api.get("/receitas/exportar", {
        responseType: "blob",
        params: {
          formato,
          categoriaId: categoriaId ? Number(categoriaId) : undefined,
          contaId: contaId ? Number(contaId) : undefined,
          recebida: recebida ? recebida === "true" : undefined,
        },
      });
      downloadBlob(response.data, `receitas.${formato}`);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    } finally {
      setIsExporting(false);
    }
  }

  async function handleDelete(income: Income) {
    if (!window.confirm(`Excluir a receita "${income.descricao}"?`)) return;
    setActionError(null);
    try {
      await deleteIncome.mutateAsync(income.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleMarkReceived(income: Income) {
    setActionError(null);
    try {
      await markReceived.mutateAsync({ id: income.id, payload: { dataRecebimento: todayIsoDate() } });
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">{title ?? "Receitas"}</h1>
          <p className="text-sm text-slate-500">
            {description ?? "Entradas de dinheiro registradas nas suas contas."}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" isLoading={isExporting} onClick={() => handleExport("xlsx")}>
            Exportar Excel
          </Button>
          <Button variant="secondary" isLoading={isExporting} onClick={() => handleExport("pdf")}>
            Exportar PDF
          </Button>
          {!isViewer && (
            <Button onClick={() => setModalState({ open: true, income: null })}>+ Nova receita</Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
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
        <Select
          label="Filtrar por status"
          value={recebida}
          onChange={(e) => {
            setRecebida(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Todas</option>
          <option value="true">Recebidas</option>
          <option value="false">Pendentes</option>
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
                  <th className="px-4 py-3 font-medium">Status</th>
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
                      <Badge tone={income.recebida ? "green" : "amber"}>
                        {income.recebida ? "Recebida" : "Pendente"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        {!income.recebida && (
                          <Button
                            variant="secondary"
                            onClick={() => handleMarkReceived(income)}
                            isLoading={markReceived.isPending}
                          >
                            Marcar recebida
                          </Button>
                        )}
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
