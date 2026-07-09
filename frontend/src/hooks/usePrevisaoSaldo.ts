import { useQuery } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { PrevisaoSaldo } from "@/types/previsaoSaldo";

const KEY = "previsaoSaldo";

export function usePrevisaoSaldo(mesesHistorico = 6, mesesProjecao = 12) {
  return useQuery({
    queryKey: [KEY, mesesHistorico, mesesProjecao],
    queryFn: async () => {
      const { data } = await api.get<PrevisaoSaldo>("/previsao-saldo", {
        params: { mesesHistorico, mesesProjecao },
      });
      return data;
    },
  });
}
