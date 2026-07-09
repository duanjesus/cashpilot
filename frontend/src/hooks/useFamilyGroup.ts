import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type {
  ChangeMemberRoleRequest,
  CreateFamilyGroupRequest,
  FamilyGroupResponseDTO,
  FamilyInviteResponseDTO,
  InviteMemberRequest,
} from "@/types/familyGroup";

const KEY = "myFamilyGroup";
const INVITES_KEY = "pendingFamilyInvites";

export function useMyFamilyGroup() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<FamilyGroupResponseDTO>("/grupos-familiares/me");
      return data ? data : null;
    },
  });
}

export function usePendingInvites() {
  return useQuery({
    queryKey: [INVITES_KEY],
    queryFn: async () => {
      const { data } = await api.get<FamilyInviteResponseDTO[]>("/grupos-familiares/convites/pendentes");
      return data;
    },
  });
}

function invalidateAffected(queryClient: ReturnType<typeof useQueryClient>) {
  queryClient.invalidateQueries({ queryKey: [KEY] });
  queryClient.invalidateQueries({ queryKey: [INVITES_KEY] });
}

export function useCreateFamilyGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CreateFamilyGroupRequest) => {
      const { data } = await api.post<FamilyGroupResponseDTO>("/grupos-familiares", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeleteFamilyGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      await api.delete("/grupos-familiares");
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useInviteMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: InviteMemberRequest) => {
      const { data } = await api.post<FamilyInviteResponseDTO>("/grupos-familiares/convites", payload);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useAcceptInvite() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      const { data } = await api.patch<FamilyGroupResponseDTO>(`/grupos-familiares/convites/${id}/aceitar`);
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useDeclineInvite() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.patch(`/grupos-familiares/convites/${id}/recusar`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useChangeMemberRole() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ userId, payload }: { userId: number; payload: ChangeMemberRoleRequest }) => {
      const { data } = await api.patch<FamilyGroupResponseDTO>(
        `/grupos-familiares/membros/${userId}/papel`,
        payload,
      );
      return data;
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}

export function useRemoveMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (userId: number) => {
      await api.delete(`/grupos-familiares/membros/${userId}`);
    },
    onSuccess: () => invalidateAffected(queryClient),
  });
}
