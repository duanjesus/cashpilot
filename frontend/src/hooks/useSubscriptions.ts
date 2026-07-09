import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Expense } from "@/types/expense";
import type { Subscription, SubscriptionRequest } from "@/types/subscription";

const KEY = "subscriptions";

export function useSubscriptions() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<Subscription[]>("/assinaturas");
      return data;
    },
  });
}

export function useSubscription(id: number | null) {
  return useQuery({
    queryKey: [KEY, id],
    queryFn: async () => {
      const { data } = await api.get<Subscription>(`/assinaturas/${id}`);
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

export function useCreateSubscription() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: SubscriptionRequest) => {
      const { data } = await api.post<Subscription>("/assinaturas", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useUpdateSubscription() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: SubscriptionRequest }) => {
      const { data } = await api.put<Subscription>(`/assinaturas/${id}`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteSubscription() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/assinaturas/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useGerarPendentes() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const { data } = await api.post<Expense[]>("/assinaturas/gerar-pendentes");
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
