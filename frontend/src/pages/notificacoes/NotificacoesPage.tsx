import { useState } from "react";

import {
  useDeleteNotification,
  useMarkAllAsRead,
  useMarkAsRead,
  useNotifications,
} from "@/hooks/useNotifications";
import { usePaginationState } from "@/hooks/usePaginationState";
import { extractErrorMessage } from "@/lib/api";
import { NOTIFICATION_TIPO_LABELS } from "@/types/notification";
import { formatDateTime } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Pagination } from "@/components/ui/Pagination";

export function NotificacoesPage() {
  const { page, size, setPage } = usePaginationState();
  const [somenteNaoLidas, setSomenteNaoLidas] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const { data, isLoading, isError } = useNotifications(somenteNaoLidas ? false : undefined, page, size);
  const markAsRead = useMarkAsRead();
  const markAllAsRead = useMarkAllAsRead();
  const deleteNotification = useDeleteNotification();

  async function handleMarkAsRead(id: number) {
    setActionError(null);
    try {
      await markAsRead.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleMarkAllAsRead() {
    setActionError(null);
    try {
      await markAllAsRead.mutateAsync();
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm("Excluir esta notificação?")) return;
    setActionError(null);
    try {
      await deleteNotification.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Notificações</h1>
          <p className="text-sm text-slate-500">Avisos sobre contas a vencer, faturas e metas atingidas.</p>
        </div>
        <div className="flex items-center gap-2">
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              className="h-4 w-4 rounded border-slate-300"
              checked={somenteNaoLidas}
              onChange={(e) => {
                setSomenteNaoLidas(e.target.checked);
                setPage(0);
              }}
            />
            Somente não lidas
          </label>
          <Button variant="secondary" onClick={handleMarkAllAsRead} isLoading={markAllAsRead.isPending}>
            Marcar todas como lidas
          </Button>
        </div>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as notificações." />}
        {!isLoading && data?.content.length === 0 && <EmptyState message="Nenhuma notificação encontrada." />}
        {!isLoading && data && data.content.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Tipo</th>
                  <th className="px-4 py-3 font-medium">Mensagem</th>
                  <th className="px-4 py-3 font-medium">Data</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.content.map((notification) => (
                  <tr key={notification.id}>
                    <td className="px-4 py-3 text-slate-600">{NOTIFICATION_TIPO_LABELS[notification.tipo]}</td>
                    <td className="px-4 py-3 font-medium text-slate-900">{notification.mensagem}</td>
                    <td className="px-4 py-3 text-slate-600">{formatDateTime(notification.createdAt)}</td>
                    <td className="px-4 py-3">
                      <Badge tone={notification.lida ? "slate" : "blue"}>
                        {notification.lida ? "Lida" : "Não lida"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        {!notification.lida && (
                          <Button
                            variant="secondary"
                            onClick={() => handleMarkAsRead(notification.id)}
                            isLoading={markAsRead.isPending}
                          >
                            Marcar como lida
                          </Button>
                        )}
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(notification.id)}
                          isLoading={deleteNotification.isPending}
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
    </div>
  );
}
