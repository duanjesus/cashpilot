import { useMutation } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { SimulacaoCompararRequest, SimulacaoCompararResponse } from "@/types/simulacao";

export function useCompararSimulacoes() {
  return useMutation({
    mutationFn: async (payload: SimulacaoCompararRequest) => {
      const { data } = await api.post<SimulacaoCompararResponse>("/simulacoes/comparar", payload);
      return data;
    },
  });
}
