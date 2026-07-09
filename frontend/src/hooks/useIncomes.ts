import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Page } from "@/types/common";
import type { Income, IncomeFilters, IncomeRequest, MarkIncomeReceivedRequest } from "@/types/income";

const KEY = "incomes";

export function useIncomes(filters: IncomeFilters) {
  return useQuery({
    queryKey: [KEY, filters],
    queryFn: async () => {
      const { data } = await api.get<Page<Income>>("/receitas", { params: filters });
      return data;
    },
  });
}

export function useIncome(id: number | null) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: async () => {
      const { data } = await api.get<Income>(`/receitas/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] });
  queryClient.invalidateQueries({ queryKey: [DASHBOARD_KEY] });
}

export function useCreateIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: IncomeRequest) => {
      const { data } = await api.post<Income>("/receitas", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useUpdateIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: IncomeRequest }) => {
      const { data } = await api.put<Income>(`/receitas/${id}`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteIncome() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/receitas/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useMarkIncomeReceived() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: MarkIncomeReceivedRequest }) => {
      const { data } = await api.patch<Income>(`/receitas/${id}/receber`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
