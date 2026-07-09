import { useState } from "react";

import { useBankAccounts, useDeleteBankAccount } from "@/hooks/useBankAccounts";
import { useMyFamilyGroup } from "@/hooks/useFamilyGroup";
import { extractErrorMessage } from "@/lib/api";
import type { BankAccount } from "@/types/bankAccount";
import { BANK_ACCOUNT_TYPE_LABELS } from "@/types/bankAccount";
import { formatCurrency } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { ContaFormModal } from "@/pages/contas/ContaFormModal";

export function ContasListPage() {
  const { data, isLoading, isError } = useBankAccounts();
  const { data: familyGroup } = useMyFamilyGroup();
  const isViewer = familyGroup?.papelDoUsuarioAtual === "VIEWER";
  const deleteAccount = useDeleteBankAccount();

  const [modalState, setModalState] = useState<{ open: boolean; account: BankAccount | null }>({
    open: false,
    account: null,
  });
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(account: BankAccount) {
    if (!window.confirm(`Excluir a conta "${account.nome}"?`)) return;
    setActionError(null);
    try {
      await deleteAccount.mutateAsync(account.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Contas bancárias</h1>
          <p className="text-sm text-slate-500">Contas e carteiras usadas para registrar receitas e despesas.</p>
        </div>
        {!isViewer && (
          <Button onClick={() => setModalState({ open: true, account: null })}>+ Nova conta</Button>
        )}
      </div>

      <ErrorBanner message={actionError} />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex justify-center p-10">
            <Spinner />
          </div>
        )}
        {isError && <ErrorBanner message="Não foi possível carregar as contas." />}
        {!isLoading && data?.length === 0 && <EmptyState message="Nenhuma conta cadastrada ainda." />}
        {!isLoading && data && data.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Nome</th>
                  <th className="px-4 py-3 font-medium">Instituição</th>
                  <th className="px-4 py-3 font-medium">Tipo</th>
                  <th className="px-4 py-3 font-medium">Saldo atual</th>
                  <th className="px-4 py-3 font-medium">Origem</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.map((account) => (
                  <tr key={account.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">{account.nome}</td>
                    <td className="px-4 py-3 text-slate-600">{account.instituicao}</td>
                    <td className="px-4 py-3 text-slate-600">{BANK_ACCOUNT_TYPE_LABELS[account.tipo]}</td>
                    <td className="px-4 py-3 text-slate-600">{formatCurrency(account.saldoAtual)}</td>
                    <td className="px-4 py-3">
                      {account.origem === "OPEN_FINANCE" && <Badge tone="blue">Open Finance</Badge>}
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={account.ativa ? "green" : "slate"}>{account.ativa ? "Ativa" : "Inativa"}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button variant="secondary" onClick={() => setModalState({ open: true, account })}>
                          Editar
                        </Button>
                        <Button
                          variant="danger"
                          onClick={() => handleDelete(account)}
                          isLoading={deleteAccount.isPending}
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
        <ContaFormModal account={modalState.account} onClose={() => setModalState({ open: false, account: null })} />
      )}
    </div>
  );
}
