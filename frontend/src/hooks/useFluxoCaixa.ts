import { useQuery } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { FluxoCaixa } from "@/types/fluxoCaixa";

const KEY = "fluxoCaixa";

export function useFluxoCaixa(dias = 30) {
  return useQuery({
    queryKey: [KEY, dias],
    queryFn: async () => {
      const { data } = await api.get<FluxoCaixa>("/fluxo-caixa", { params: { dias } });
      return data;
    },
  });
}
