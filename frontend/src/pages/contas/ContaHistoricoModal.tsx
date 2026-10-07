import { useState } from "react";

import { useBankAccountBalanceHistory } from "@/hooks/useBankAccounts";
import type { BankAccount } from "@/types/bankAccount";
import { formatCurrency } from "@/utils/format";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { SaldoHistoricoChart } from "@/components/SaldoHistoricoChart";

interface ContaHistoricoModalProps {
  account: BankAccount;
  onClose: () => void;
}

export function ContaHistoricoModal({ account, onClose }: ContaHistoricoModalProps) {
  const [dias, setDias] = useState(30);
  const { data, isLoading, isError } = useBankAccountBalanceHistory(account.id, dias);

  return (
    <Modal title={`Histórico de saldo: ${account.nome}`} isOpen onClose={onClose}>
      <div className="mb-3 flex items-end justify-between gap-4">
        <div>
          <p className="text-xs text-slate-500">Saldo atual</p>
          <p className="text-lg font-semibold text-slate-900">{formatCurrency(account.saldoAtual)}</p>
        </div>
        <Select aria-label="Período" value={String(dias)} onChange={(e) => setDias(Number(e.target.value))}>
          <option value="30">30 dias</option>
          <option value="90">90 dias</option>
          <option value="365">12 meses</option>
        </Select>
      </div>

      {isLoading && (
        <div className="flex justify-center p-10">
          <Spinner />
        </div>
      )}
      {isError && <ErrorBanner message="Não foi possível carregar o histórico de saldo." />}
      {!isLoading && data && data.length === 0 && (
        <EmptyState message="Esta conta ainda não tem saldo no período: a data do saldo inicial é futura." />
      )}
      {!isLoading && data && data.length > 0 && <SaldoHistoricoChart data={data} height={220} />}
    </Modal>
  );
}
