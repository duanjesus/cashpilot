import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import { DASHBOARD_KEY } from "@/hooks/useDashboard";
import type { Goal, GoalRequest, UpdateGoalProgressRequest } from "@/types/goal";

const KEY = "goals";

export function useGoals() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<Goal[]>("/metas");
      return data;
    },
  });
}

export function useGoal(id: number | null) {
  return useQuery({
    queryKey: [KEY, id],
    queryFn: async () => {
      const { data } = await api.get<Goal>(`/metas/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: [DASHBOARD_KEY] });
}

export function useCreateGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: GoalRequest) => {
      const { data } = await api.post<Goal>("/metas", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useUpdateGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: GoalRequest }) => {
      const { data } = await api.put<Goal>(`/metas/${id}`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useUpdateGoalProgress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: UpdateGoalProgressRequest }) => {
      const { data } = await api.patch<Goal>(`/metas/${id}/progresso`, payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteGoal() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/metas/${id}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
