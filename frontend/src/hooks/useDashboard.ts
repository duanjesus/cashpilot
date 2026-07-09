import { useQuery } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { BalanceEvolutionPoint, DashboardSummary } from "@/types/dashboard";

export const DASHBOARD_KEY = "dashboard";

export function useDashboardSummary() {
  return useQuery({
    queryKey: [DASHBOARD_KEY, "resumo"],
    queryFn: async () => {
      const { data } = await api.get<DashboardSummary>("/dashboard/resumo");
      return data;
    },
  });
}

export function useBalanceEvolution(dias = 30) {
  return useQuery({
    queryKey: [DASHBOARD_KEY, "evolucao-saldo", dias],
    queryFn: async () => {
      const { data } = await api.get<BalanceEvolutionPoint[]>("/dashboard/evolucao-saldo", {
        params: { dias },
      });
      return data;
    },
  });
}
