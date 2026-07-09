import { useQuery } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { RelatorioMensal } from "@/types/relatorio";

const KEY = "relatorios";

export function useRelatorioMensal(meses = 12) {
  return useQuery({
    queryKey: [KEY, "mensal", meses],
    queryFn: async () => {
      const { data } = await api.get<RelatorioMensal[]>("/relatorios/mensal", { params: { meses } });
      return data;
    },
  });
}
