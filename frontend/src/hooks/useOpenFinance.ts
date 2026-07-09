import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { BANK_ACCOUNTS_KEY } from "@/hooks/useBankAccounts";
import type { BankAccount } from "@/types/bankAccount";
import type { CreditCard } from "@/types/creditCard";
import type { ConnectInstitutionRequest, OpenFinanceInstitution } from "@/types/openFinance";

const CREDIT_CARDS_KEY = "creditCards";
const KEY = "openFinanceInstitutions";

export function useOpenFinanceInstitutions() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<OpenFinanceInstitution[]>("/open-finance/instituicoes");
      return data;
    },
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [BANK_ACCOUNTS_KEY] });
  queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_KEY] });
}

export function useConnectInstitution() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: ConnectInstitutionRequest) => {
      const { data } = await api.post<BankAccount | CreditCard>("/open-finance/conectar", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useSyncBankAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.patch<BankAccount>(`/open-finance/contas/${id}/sincronizar`);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useSyncCreditCard() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.patch<CreditCard>(`/open-finance/cartoes/${id}/sincronizar`);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
