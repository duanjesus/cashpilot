import { useState } from "react";

import { useBankAccounts } from "@/hooks/useBankAccounts";
import { useCreditCards } from "@/hooks/useCreditCards";
import { useOpenFinanceInstitutions, useSyncBankAccount, useSyncCreditCard } from "@/hooks/useOpenFinance";
import { extractErrorMessage } from "@/lib/api";
import type { OpenFinanceInstitution } from "@/types/openFinance";
import { formatCurrency, formatDateTime } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { ConnectInstitutionModal } from "@/pages/open-finance/ConnectInstitutionModal";

export function OpenFinancePage() {
  const { data: institutions, isLoading: isLoadingInstitutions } = useOpenFinanceInstitutions();
  const { data: accounts, isLoading: isLoadingAccounts } = useBankAccounts();
  const { data: cards, isLoading: isLoadingCards } = useCreditCards();
  const syncAccount = useSyncBankAccount();
  const syncCard = useSyncCreditCard();

  const [connectingInstitution, setConnectingInstitution] = useState<OpenFinanceInstitution | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const connectedAccounts = accounts?.filter((account) => account.origem === "OPEN_FINANCE") ?? [];
  const connectedCards = cards?.filter((card) => card.origem === "OPEN_FINANCE") ?? [];

  async function handleSyncAccount(id: number) {
    setActionError(null);
    try {
      await syncAccount.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleSyncCard(id: number) {
    setActionError(null);
    try {
      await syncCard.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Open Finance</h1>
        <p className="text-sm text-slate-500">Conecte instituições financeiras fictícias para testar a integração.</p>
      </div>

      <div className="rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-sm text-blue-800">
        Esta é uma demonstração da estrutura para Open Finance — nenhuma integração bancária real é feita.
      </div>

      <ErrorBanner message={actionError} />

      <div>
        <h2 className="mb-3 font-semibold text-slate-900">Instituições disponíveis</h2>
        {isLoadingInstitutions && (
          <div className="flex justify-center p-6">
            <Spinner />
          </div>
        )}
        {!isLoadingInstitutions && institutions?.length === 0 && (
          <EmptyState message="Nenhuma instituição disponível no momento." />
        )}
        {!isLoadingInstitutions && institutions && institutions.length > 0 && (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {institutions.map((institution) => (
              <div
                key={institution.id}
                className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
              >
                <span className="font-medium text-slate-900">{institution.nome}</span>
                <Button variant="secondary" onClick={() => setConnectingInstitution(institution)}>
                  Conectar
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div>
        <h2 className="mb-3 font-semibold text-slate-900">Contas conectadas</h2>
        {isLoadingAccounts && (
          <div className="flex justify-center p-6">
            <Spinner />
          </div>
        )}
        {!isLoadingAccounts && connectedAccounts.length === 0 && (
          <EmptyState message="Nenhuma conta conectada via Open Finance ainda." />
        )}
        {!isLoadingAccounts && connectedAccounts.length > 0 && (
          <div className="flex flex-col gap-2">
            {connectedAccounts.map((account) => (
              <div
                key={account.id}
                className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
              >
                <div>
                  <p className="font-medium text-slate-900">{account.nome}</p>
                  <p className="text-sm text-slate-500">
                    {account.instituicaoNome} · Saldo atual: {formatCurrency(account.saldoAtual)}
                  </p>
                  <p className="text-xs text-slate-400">
                    Última sincronização:{" "}
                    {account.ultimaSincronizacao ? formatDateTime(account.ultimaSincronizacao) : "nunca"}
                  </p>
                </div>
                <Button
                  variant="secondary"
                  onClick={() => handleSyncAccount(account.id)}
                  isLoading={syncAccount.isPending}
                >
                  Sincronizar
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div>
        <h2 className="mb-3 font-semibold text-slate-900">Cartões conectados</h2>
        {isLoadingCards && (
          <div className="flex justify-center p-6">
            <Spinner />
          </div>
        )}
        {!isLoadingCards && connectedCards.length === 0 && (
          <EmptyState message="Nenhum cartão conectado via Open Finance ainda." />
        )}
        {!isLoadingCards && connectedCards.length > 0 && (
          <div className="flex flex-col gap-2">
            {connectedCards.map((card) => (
              <div
                key={card.id}
                className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
              >
                <div>
                  <p className="font-medium text-slate-900">{card.nome}</p>
                  <p className="text-sm text-slate-500">
                    {card.instituicaoNome} · Fatura atual: {formatCurrency(card.faturaAtual)}
                  </p>
                  <p className="text-xs text-slate-400">
                    Última sincronização: {card.ultimaSincronizacao ? formatDateTime(card.ultimaSincronizacao) : "nunca"}
                  </p>
                </div>
                <Button variant="secondary" onClick={() => handleSyncCard(card.id)} isLoading={syncCard.isPending}>
                  Sincronizar
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>

      {connectingInstitution && (
        <ConnectInstitutionModal institution={connectingInstitution} onClose={() => setConnectingInstitution(null)} />
      )}
    </div>
  );
}
