import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Page } from "@/types/common";
import type { Expense, ExpenseFilters, ExpenseRequest, MarkExpensePaidRequest } from "@/types/expense";

const KEY = "expenses";

export function useExpenses(filters: ExpenseFilters) {
  return useQuery({
    queryKey: [KEY, filters],
    queryFn: async () => {
      const { data } = await api.get<Page<Expense>>("/despesas", { params: filters });
      return data;
    },
  });
}

export function useExpense(id: number | null) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: async () => {
      const { data } = await api.get<Expense>(`/despesas/${id}`);
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

export function useCreateExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ExpenseRequest) => {
      const { data } = await api.post<Expense>("/despesas", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useUpdateExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: ExpenseRequest }) => {
      const { data } = await api.put<Expense>(`/despesas/${id}`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteExpense() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/despesas/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useMarkExpensePaid() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: MarkExpensePaidRequest }) => {
      const { data } = await api.patch<Expense>(`/despesas/${id}/pagar`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
