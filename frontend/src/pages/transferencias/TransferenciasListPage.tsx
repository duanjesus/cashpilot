import { useState } from "react";

import { useDeleteTransfer, useTransfers } from "@/hooks/useTransfers";
import { usePaginationState } from "@/hooks/usePaginationState";
import { extractErrorMessage } from "@/lib/api";
import type { Transfer } from "@/types/transfer";
import { formatCurrency, formatDate } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";
import { TransferenciaFormModal } from "@/pages/transferencias/TransferenciaFormModal";

export function TransferenciasListPage() {
  const { page, size, setPage } = usePaginationState();
  const { data, isLoading, isError } = useTransfers({ page, size });
  const deleteTransfer = useDeleteTransfer();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(transfer: Transfer) {
    if (!window.confirm("Excluir esta transferência?")) return;
    setActionError(null);
    try {
      await deleteTransfer.mutateAsync(transfer.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Transferências</h1>
          <p className="text-sm text-slate-500">
            Movimentações entre suas próprias contas. Registros são apenas criados ou excluídos, nunca editados.
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>+ Nova transferência</Button>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as transferências." />}
        {!isLoading && data?.content.length === 0 && (
          <EmptyState message="Nenhuma transferência registrada ainda." />
        )}
        {!isLoading && data && data.content.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Origem</th>
                  <th className="px-4 py-3 font-medium">Destino</th>
                  <th className="px-4 py-3 font-medium">Data</th>
                  <th className="px-4 py-3 font-medium">Descrição</th>
                  <th className="px-4 py-3 font-medium">Valor</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((transfer) => (
                  <tr key={transfer.id}>
                    <td className="px-4 py-3 text-slate-600">{transfer.contaOrigemNome}</td>
                    <td className="px-4 py-3 text-slate-600">{transfer.contaDestinoNome}</td>
                    <td className="px-4 py-3 text-slate-600">{formatDate(transfer.data)}</td>
                    <td className="px-4 py-3 text-slate-600">{transfer.descricao ?? "—"}</td>
                    <td className="px-4 py-3 font-medium text-slate-900">{formatCurrency(transfer.valor)}</td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(transfer)}
                          isLoading={deleteTransfer.isPending}
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

      {isModalOpen && <TransferenciaFormModal onClose={() => setIsModalOpen(false)} />}
    </div>
  );
}
