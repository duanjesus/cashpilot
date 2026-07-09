import { useState } from "react";

import { useDeleteParcelamento, useParcelamentos } from "@/hooks/useParcelamentos";
import { usePaginationState } from "@/hooks/usePaginationState";
import { extractErrorMessage } from "@/lib/api";
import type { Parcelamento } from "@/types/parcelamento";
import { formatCurrency, formatDate } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";
import { ParcelamentoFormModal } from "@/pages/parcelamentos/ParcelamentoFormModal";

function paymentSource(parcelamento: Parcelamento) {
  if (parcelamento.cartaoCreditoNome) return `Cartão: ${parcelamento.cartaoCreditoNome}`;
  if (parcelamento.contaBancariaNome) return `Conta: ${parcelamento.contaBancariaNome}`;
  return "—";
}

export function ParcelamentosListPage() {
  const { page, size, setPage } = usePaginationState();
  const { data, isLoading, isError } = useParcelamentos({ page, size });
  const deleteParcelamento = useDeleteParcelamento();

  const [modalOpen, setModalOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(parcelamento: Parcelamento) {
    if (!window.confirm(`Excluir o parcelamento "${parcelamento.descricao}"?`)) return;
    setActionError(null);
    try {
      await deleteParcelamento.mutateAsync(parcelamento.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Parcelamentos</h1>
          <p className="text-sm text-slate-500">Compras parceladas em conta ou cartão de crédito.</p>
        </div>
        <Button onClick={() => setModalOpen(true)}>+ Novo parcelamento</Button>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar os parcelamentos." />}
        {!isLoading && data?.content.length === 0 && (
          <EmptyState message="Nenhum parcelamento registrado ainda." />
        )}
        {!isLoading && data && data.content.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Descrição</th>
                  <th className="px-4 py-3 font-medium">Categoria</th>
                  <th className="px-4 py-3 font-medium">Pagamento</th>
                  <th className="px-4 py-3 font-medium">1ª parcela</th>
                  <th className="px-4 py-3 font-medium">Valor total</th>
                  <th className="px-4 py-3 font-medium">Progresso</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((parcelamento) => {
                  const progresso =
                    parcelamento.valorTotal > 0
                      ? Math.min(100, Math.max(0, (parcelamento.valorPago / parcelamento.valorTotal) * 100))
                      : 0;
                  return (
                    <tr key={parcelamento.id}>
                      <td className="px-4 py-3 font-medium text-slate-900">{parcelamento.descricao}</td>
                      <td className="px-4 py-3 text-slate-600">{parcelamento.categoriaNome}</td>
                      <td className="px-4 py-3 text-slate-600">{paymentSource(parcelamento)}</td>
                      <td className="px-4 py-3 text-slate-600">{formatDate(parcelamento.dataPrimeiraParcela)}</td>
                      <td className="px-4 py-3 font-medium text-red-700">
                        {formatCurrency(parcelamento.valorTotal)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-col gap-1">
                          <span className="text-xs text-slate-600">
                            {parcelamento.parcelasPagas}/{parcelamento.numeroParcelas} pagas
                          </span>
                          <div className="h-1.5 w-28 overflow-hidden rounded-full bg-slate-100">
                            <div
                              className="h-full rounded-full bg-brand-600"
                              style={{ width: `${progresso}%` }}
                            />
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tone={parcelamento.quitado ? "green" : "amber"}>
                          {parcelamento.quitado ? "Quitado" : "Em andamento"}
                        </Badge>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="danger"
                            onClick={() => handleDelete(parcelamento)}
                            isLoading={deleteParcelamento.isPending}
                          >
                            Excluir
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      {modalOpen && <ParcelamentoFormModal onClose={() => setModalOpen(false)} />}
    </div>
  );
}
