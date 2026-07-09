import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { Page } from "@/types/common";
import type { NotificationResponseDTO } from "@/types/notification";

const KEY = "notifications";
const UNREAD_COUNT_KEY = "unreadNotificationsCount";

export function useUnreadCount() {
  return useQuery({
    queryKey: [UNREAD_COUNT_KEY],
    queryFn: async () => {
      const { data } = await api.get<{ contagem: number }>("/notificacoes/nao-lidas/contagem");
      return data.contagem;
    },
    refetchInterval: 30000,
  });
}

export function useNotifications(lida: boolean | undefined, page: number, size: number) {
  return useQuery({
    queryKey: [KEY, { lida, page, size }],
    queryFn: async () => {
      const { data } = await api.get<Page<NotificationResponseDTO>>("/notificacoes", {
        params: { lida, page, size },
      });
      return data;
    },
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: [UNREAD_COUNT_KEY] });
}

export function useMarkAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.patch<NotificationResponseDTO>(`/notificacoes/${id}/marcar-lida`);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useMarkAllAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      await api.patch("/notificacoes/marcar-todas-lidas");
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/notificacoes/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useGenerateNotifications() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const { data } = await api.post<NotificationResponseDTO[]>("/notificacoes/gerar");
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
