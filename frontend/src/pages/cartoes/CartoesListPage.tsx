import { useState } from "react";

import { useCreditCards, useDeleteCreditCard } from "@/hooks/useCreditCards";
import { extractErrorMessage } from "@/lib/api";
import type { CreditCard } from "@/types/creditCard";
import { CARD_BRAND_LABELS } from "@/types/creditCard";
import { formatCurrency } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { CartaoFormModal } from "@/pages/cartoes/CartaoFormModal";

export function CartoesListPage() {
  const { data, isLoading, isError } = useCreditCards();
  const deleteCard = useDeleteCreditCard();

  const [modalState, setModalState] = useState<{ open: boolean; card: CreditCard | null }>({
    open: false,
    card: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(card: CreditCard) {
    if (!window.confirm(`Excluir o cartão "${card.nome}"?`)) return;
    setActionError(null);
    try {
      await deleteCard.mutateAsync(card.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Cartões de crédito</h1>
          <p className="text-sm text-slate-500">Cartões usados para registrar despesas.</p>
        </div>
        <Button onClick={() => setModalState({ open: true, card: null })}>+ Novo cartão</Button>
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar os cartões." />}
        {!isLoading && data?.length === 0 && <EmptyState message="Nenhum cartão cadastrado ainda." />}
        {!isLoading && data && data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Nome</th>
                  <th className="px-4 py-3 font-medium">Bandeira</th>
                  <th className="px-4 py-3 font-medium">Limite</th>
                  <th className="px-4 py-3 font-medium">Fatura atual</th>
                  <th className="px-4 py-3 font-medium">Origem</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((card) => (
                  <tr key={card.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">{card.nome}</td>
                    <td className="px-4 py-3 text-slate-600">{CARD_BRAND_LABELS[card.bandeira]}</td>
                    <td className="px-4 py-3 text-slate-600">{formatCurrency(card.limite)}</td>
                    <td className="px-4 py-3 text-slate-600">{formatCurrency(card.faturaAtual)}</td>
                    <td className="px-4 py-3">
                      {card.origem === "OPEN_FINANCE" && <Badge tone="blue">Open Finance</Badge>}
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={card.ativo ? "green" : "slate"}>{card.ativo ? "Ativo" : "Inativo"}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button variant="secondary" onClick={() => setModalState({ open: true, card })}>
                          Editar
                        </Button>
                        <Button variant="danger" onClick={() => handleDelete(card)} isLoading={deleteCard.isPending}>
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
        <CartaoFormModal card={modalState.card} onClose={() => setModalState({ open: false, card: null })} />
      )}
    </div>
  );
}
