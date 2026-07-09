import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { CreditCard, CreditCardRequest } from "@/types/creditCard";

const KEY = "creditCards";

export function useCreditCards() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<CreditCard[]>("/cartoes");
      return data;
    },
  });
}

export function useCreditCard(id: number | null) {
  return useQuery({
    queryKey: [KEY, id],
    queryFn: async () => {
      const { data } = await api.get<CreditCard>(`/cartoes/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

export function useCreateCreditCard() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreditCardRequest) => {
      const { data } = await api.post<CreditCard>("/cartoes", payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateCreditCard() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: CreditCardRequest }) => {
      const { data } = await api.put<CreditCard>(`/cartoes/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useDeleteCreditCard() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/cartoes/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}
