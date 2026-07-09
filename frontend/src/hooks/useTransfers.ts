import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Page } from "@/types/common";
import type { Transfer, TransferFilters, TransferRequest } from "@/types/transfer";

const KEY = "transfers";

export function useTransfers(filters: TransferFilters) {
  return useQuery({
    queryKey: [KEY, filters],
    queryFn: async () => {
      const { data } = await api.get<Page<Transfer>>("/transferencias", { params: filters });
      return data;
    },
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] });
  queryClient.invalidateQueries({ queryKey: [DASHBOARD_KEY] });
}

export function useCreateTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: TransferRequest) => {
      const { data } = await api.post<Transfer>("/transferencias", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteTransfer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/transferencias/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
