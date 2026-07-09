import { useState } from "react";

import { useDeleteGoal, useGoals } from "@/hooks/useGoals";
import { extractErrorMessage } from "@/lib/api";
import type { Goal } from "@/types/goal";
import { formatCurrency, formatDate, formatPercent } from "@/utils/format";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Spinner } from "@/components/ui/Spinner";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { MetaFormModal } from "@/pages/metas/MetaFormModal";
import { MetaProgressModal } from "@/pages/metas/MetaProgressModal";

export function MetasListPage() {
  const { data, isLoading, isError } = useGoals();
  const deleteGoal = useDeleteGoal();

  const [modalState, setModalState] = useState<{ open: boolean; goal: Goal | null }>({
    open: false,
    goal: null,
  });
  const [progressGoal, setProgressGoal] = useState<Goal | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleDelete(goal: Goal) {
    if (!window.confirm(`Excluir a meta "${goal.nome}"?`)) return;
    setActionError(null);
    try {
      await deleteGoal.mutateAsync(goal.id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Metas financeiras</h1>
          <p className="text-sm text-slate-500">Objetivos de valor que você está acompanhando.</p>
        </div>
        <Button onClick={() => setModalState({ open: true, goal: null })}>+ Nova meta</Button>
      </div>

      <ErrorBanner message={actionError} />

      {isLoading && (
        <div className="flex justify-center p-10">
          <Spinner />
        </div>
      )}
      {isError && <ErrorBanner message="Não foi possível carregar as metas." />}
      {!isLoading && data?.length === 0 && <EmptyState message="Nenhuma meta cadastrada ainda." />}

      {!isLoading && data && data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((goal) => (
            <div key={goal.id} className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <div className="flex items-start justify-between">
                <h2 className="font-semibold text-slate-900">{goal.nome}</h2>
                <Badge tone={goal.ativa ? "green" : "slate"}>{goal.ativa ? "Ativa" : "Inativa"}</Badge>
              </div>
              <div className="text-sm text-slate-500">
                Alvo: {formatCurrency(goal.valorAlvo)} até {formatDate(goal.dataAlvo)}
              </div>
              <div className="text-sm text-slate-700">
                Atual: <span className="font-medium">{formatCurrency(goal.valorAtual)}</span>
              </div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full bg-brand-600"
                  style={{ width: `${Math.min(100, Math.max(0, goal.progresso))}%` }}
                />
              </div>
              <div className="text-xs text-slate-500">{formatPercent(goal.progresso)} concluído</div>
              <div className="mt-2 flex flex-wrap justify-end gap-2">
                <Button variant="secondary" onClick={() => setProgressGoal(goal)}>
                  Atualizar progresso
                </Button>
                <Button variant="secondary" onClick={() => setModalState({ open: true, goal })}>
                  Editar
                </Button>
                <Button variant="danger" onClick={() => handleDelete(goal)} isLoading={deleteGoal.isPending}>
                  Excluir
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modalState.open && (
        <MetaFormModal goal={modalState.goal} onClose={() => setModalState({ open: false, goal: null })} />
      )}
      {progressGoal && <MetaProgressModal goal={progressGoal} onClose={() => setProgressGoal(null)} />}
    </div>
  );
}
