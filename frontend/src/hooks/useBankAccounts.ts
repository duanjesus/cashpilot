import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { BankAccount, BankAccountRequest } from "@/types/bankAccount";

export const BANK_ACCOUNTS_KEY = "bankAccounts";

export function useBankAccounts() {
  return useQuery({
    queryKey: [BANK_ACCOUNTS_KEY],
    queryFn: async () => {
      const { data } = await api.get<BankAccount[]>("/contas");
      return data;
    },
  });
}

export function useBankAccount(id: number | null) {
  return useQuery({
    queryKey: [BANK_ACCOUNTS_KEY, id],
    queryFn: async () => {
      const { data } = await api.get<BankAccount>(`/contas/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

export function useCreateBankAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: BankAccountRequest) => {
      const { data } = await api.post<BankAccount>("/contas", payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] }),
  });
}

export function useUpdateBankAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: BankAccountRequest }) => {
      const { data } = await api.put<BankAccount>(`/contas/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] }),
  });
}

export function useDeleteBankAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/contas/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] }),
  });
}
