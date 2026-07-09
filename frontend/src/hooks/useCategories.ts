import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { Category, CategoryRequest } from "@/types/category";

const KEY = "categories";

export function useCategories() {
  return useQuery({
    queryKey: [KEY],
    queryFn: async () => {
      const { data } = await api.get<Category[]>("/categorias");
      return data;
    },
  });
}

export function useCategory(id: number | null) {
  return useQuery({
    queryKey: [KEY, id],
    queryFn: async () => {
      const { data } = await api.get<Category>(`/categorias/${id}`);
      return data;
    },
    enabled: id !== null,
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: CategoryRequest) => {
      const { data } = await api.post<Category>("/categorias", payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: CategoryRequest }) => {
      const { data } = await api.put<Category>(`/categorias/${id}`, payload);
      return data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/categorias/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [KEY] }),
  });
}
