import { useState } from "react";

import { useCategories } from "@/hooks/useCategories";
import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreditCards } from "@/hooks/useCreditCards";
import { useDeleteExpense, useExpenses, useMarkExpensePaid } from "@/hooks/useExpenses";
import { usePaginationState } from "@/hooks/usePaginationState";
import { api, extractErrorMessage } from "@/lib/api";
import type { Expense, ExpenseFilters } from "@/types/expense";
import { formatCurrency, formatDate, todayIsoDate } from "@/utils/format";
import { downloadBlob } from "@/utils/download";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Select";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";
import { DespesaFormModal } from "@/pages/despesas/DespesaFormModal";

interface DespesasListPageProps {
  initialFilters?: Partial<ExpenseFilters>;
  initialSort?: "data,asc" | "data,desc";
  title?: string;
  description?: string;
}

export function DespesasListPage({ initialFilters, initialSort, title, description }: DespesasListPageProps) {
  const { page, size, setPage } = usePaginationState();
  const [categoriaId, setCategoriaId] = useState<string>("");
  const [contaId, setContaId] = useState<string>("");
  const [cartaoId, setCartaoId] = useState<string>("");
  const [paga, setPaga] = useState<string>(initialFilters?.paga === undefined ? "" : String(initialFilters.paga));

  const { data: categories } = useCategories();
  const { data: accounts } = useBankAccounts();
  const { data: cards } = useCreditCards();
  const { data, isLoading, isError } = useExpenses({
    page,
    size,
    categoriaId: categoriaId ? Number(categoriaId) : undefined,
    contaId: contaId ? Number(contaId) : undefined,
    cartaoId: cartaoId ? Number(cartaoId) : undefined,
    paga: paga ? paga === "true" : undefined,
  });
  const sortedContent = data
    ? [...data.content].sort((a, b) => {
        if (!initialSort) return 0;
        const direction = initialSort === "data,asc" ? 1 : -1;
        return a.data.localeCompare(b.data) * direction;
      })
    : [];
  const deleteExpense = useDeleteExpense();
  const markPaid = useMarkExpensePaid();

  const [modalState, setModalState] = useState<{ open: boolean; expense: Expense | null }>({
    open: false,
    expense: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);

  async function handleExport(formato: "xlsx" | "pdf") {
    setActionError(null);
    setIsExporting(true);
    try {
      const response = await api.get("/despesas/exportar", {
        responseType: "blob",
        params: {
          formato,
          categoriaId: categoriaId ? Number(categoriaId) : undefined,
          contaId: contaId ? Number(contaId) : undefined,
          cartaoId: cartaoId ? Number(cartaoId) : undefined,
          paga: paga ? paga === "true" : undefined,
        },
      });
      downloadBlob(response.data, `despesas.${formato}`);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    } finally {
      setIsExporting(false);
    }
  }

  async function handleDelete(expense: Expense) {
    if (!window.confirm(`Excluir a despesa "${expense.descricao}"?`)) return;
    setActionError(null);
    try {
      await deleteExpense.mutateAsync(expense.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleMarkPaid(expense: Expense) {
    setActionError(null);
    try {
      await markPaid.mutateAsync({ id: expense.id, payload: { dataPagamento: todayIsoDate() } });
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  function paymentSource(expense: Expense) {
    if (expense.cartaoCreditoNome) return `Cartão: ${expense.cartaoCreditoNome}`;
    if (expense.contaBancariaNome) return `Conta: ${expense.contaBancariaNome}`;
    return "—";
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">{title ?? "Despesas"}</h1>
          <p className="text-sm text-slate-500">
            {description ?? "Saídas de dinheiro pagas por conta ou cartão de crédito."}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" isLoading={isExporting} onClick={() => handleExport("xlsx")}>
            Exportar Excel
          </Button>
          <Button variant="secondary" isLoading={isExporting} onClick={() => handleExport("pdf")}>
            Exportar PDF
          </Button>
          <Button onClick={() => setModalState({ open: true, expense: null })}>+ Nova despesa</Button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
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
          label="Filtrar por cartão"
          value={cartaoId}
          onChange={(e) => {
            setCartaoId(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Todos</option>
          {cards?.map((card) => (
            <option key={card.id} value={card.id}>
              {card.nome}
            </option>
          ))}
        </Select>
        <Select
          label="Filtrar por status"
          value={paga}
          onChange={(e) => {
            setPaga(e.target.value);
            setPage(0);
          }}
        >
          <option value="">Todas</option>
          <option value="true">Pagas</option>
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
        {isError && <ErrorBanner message="Não foi possível carregar as despesas." />}
        {!isLoading && data?.content.length === 0 && <EmptyState message="Nenhuma despesa registrada ainda." />}
        {!isLoading && data && sortedContent.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Descrição</th>
                  <th className="px-4 py-3 font-medium">Categoria</th>
                  <th className="px-4 py-3 font-medium">Pagamento</th>
                  <th className="px-4 py-3 font-medium">Data</th>
                  <th className="px-4 py-3 font-medium">Valor</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sortedContent.map((expense) => (
                  <tr key={expense.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">{expense.descricao}</td>
                    <td className="px-4 py-3 text-slate-600">{expense.categoriaNome}</td>
                    <td className="px-4 py-3 text-slate-600">{paymentSource(expense)}</td>
                    <td className="px-4 py-3 text-slate-600">{formatDate(expense.data)}</td>
                    <td className="px-4 py-3 font-medium text-red-700">{formatCurrency(expense.valor)}</td>
                    <td className="px-4 py-3">
                      <Badge tone={expense.paga ? "green" : "amber"}>{expense.paga ? "Paga" : "Pendente"}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        {!expense.paga && (
                          <Button
                            variant="secondary"
                            onClick={() => handleMarkPaid(expense)}
                            isLoading={markPaid.isPending}
                          >
                            Marcar paga
                          </Button>
                        )}
                        <Button variant="secondary" onClick={() => setModalState({ open: true, expense })}>
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(expense)}
                          isLoading={deleteExpense.isPending}
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
        <DespesaFormModal
          expense={modalState.expense}
          onClose={() => setModalState({ open: false, expense: null })}
        />
      )}
    </div>
  );
}
