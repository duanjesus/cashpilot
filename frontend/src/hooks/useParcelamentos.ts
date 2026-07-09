import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Page } from "@/types/common";
import type { Parcelamento, ParcelamentoFilters, ParcelamentoRequest } from "@/types/parcelamento";

const KEY = "parcelamentos";

export function useParcelamentos(filters: ParcelamentoFilters) {
  return useQuery({
    queryKey: [KEY, filters],
    queryFn: async () => {
      const { data } = await api.get<Page<Parcelamento>>("/parcelamentos", { params: filters });
      return data;
    },
  });
}

export function useParcelamento(id: number | null) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: async () => {
      const { data } = await api.get<Parcelamento>(`/parcelamentos/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: ["expenses"] });
  queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] });
  queryClient.invalidateQueries({ queryKey: [DASHBOARD_KEY] });
}

export function useCreateParcelamento() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ParcelamentoRequest) => {
      const { data } = await api.post<Parcelamento>("/parcelamentos", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteParcelamento() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/parcelamentos/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
