import { useState } from "react";

import { useDeleteSubscription, useGerarPendentes, useSubscriptions } from "@/hooks/useSubscriptions";
import { extractErrorMessage } from "@/lib/api";
import type { Subscription } from "@/types/subscription";
import { formatCurrency, formatDate } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { AssinaturaFormModal } from "@/pages/assinaturas/AssinaturaFormModal";

function paymentSource(subscription: Subscription) {
  if (subscription.cartaoCreditoNome) return `Cartão: ${subscription.cartaoCreditoNome}`;
  if (subscription.contaBancariaNome) return `Conta: ${subscription.contaBancariaNome}`;
  return "—";
}

export function AssinaturasListPage() {
  const { data, isLoading, isError } = useSubscriptions();
  const deleteSubscription = useDeleteSubscription();
  const gerarPendentes = useGerarPendentes();

  const [modalState, setModalState] = useState<{ open: boolean; subscription: Subscription | null }>({
    open: false,
    subscription: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);

  async function handleDelete(subscription: Subscription) {
    if (!window.confirm(`Excluir a assinatura "${subscription.descricao}"?`)) return;
    setActionError(null);
    try {
      await deleteSubscription.mutateAsync(subscription.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleGerarPendentes() {
    setActionError(null);
    setInfoMessage(null);
    try {
      const generated = await gerarPendentes.mutateAsync();
      setInfoMessage(
        generated.length > 0 ? `${generated.length} cobrança(s) gerada(s).` : "Nenhuma cobrança pendente.",
      );
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Assinaturas</h1>
          <p className="text-sm text-slate-500">Cobranças recorrentes em conta ou cartão de crédito.</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={handleGerarPendentes}
            isLoading={gerarPendentes.isPending}
          >
            Gerar pendentes
          </Button>
          <Button onClick={() => setModalState({ open: true, subscription: null })}>+ Nova assinatura</Button>
        </div>
      </div>

      <ErrorBanner message={actionError} />
      {infoMessage && (
        <div className="rounded-md border border-brand-200 bg-brand-50 px-3 py-2 text-sm text-brand-700">
          {infoMessage}
        </div>
      )}

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as assinaturas." />}
        {!isLoading && data?.length === 0 && <EmptyState message="Nenhuma assinatura registrada ainda." />}
        {!isLoading && data && data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Descrição</th>
                  <th className="px-4 py-3 font-medium">Categoria</th>
                  <th className="px-4 py-3 font-medium">Pagamento</th>
                  <th className="px-4 py-3 font-medium">Dia cobrança</th>
                  <th className="px-4 py-3 font-medium">Vigência</th>
                  <th className="px-4 py-3 font-medium">Valor</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((subscription) => (
                  <tr key={subscription.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">{subscription.descricao}</td>
                    <td className="px-4 py-3 text-slate-600">{subscription.categoriaNome}</td>
                    <td className="px-4 py-3 text-slate-600">{paymentSource(subscription)}</td>
                    <td className="px-4 py-3 text-slate-600">{subscription.diaCobranca}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {formatDate(subscription.dataInicio)}
                      {subscription.dataFim ? ` – ${formatDate(subscription.dataFim)}` : " – sem término"}
                    </td>
                    <td className="px-4 py-3 font-medium text-red-700">{formatCurrency(subscription.valor)}</td>
                    <td className="px-4 py-3">
                      <Badge tone={subscription.ativa ? "green" : "slate"}>
                        {subscription.ativa ? "Ativa" : "Inativa"}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="secondary"
                          onClick={() => setModalState({ open: true, subscription })}
                        >
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(subscription)}
                          isLoading={deleteSubscription.isPending}
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
        <AssinaturaFormModal
          subscription={modalState.subscription}
          onClose={() => setModalState({ open: false, subscription: null })}
        />
      )}
    </div>
  );
}
