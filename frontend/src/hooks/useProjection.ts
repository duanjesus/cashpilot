import axios from "axios";
import { useMutation, useQuery } from "@tanstack/react-query";

import { api } from "@/lib/api";
import type { ProjectionRequest, ProjectionResult } from "@/types/projection";

/**
 * GET /projecao/ultima-simulacao returns the same shape as the POST /calcular
 * *request* body (the saved inputs), not a ProjectionResult — there's no
 * calculated result persisted, only the last set of inputs used.
 */
export function useLastSimulation() {
  return useQuery({
    queryKey: ["projection", "last"],
    queryFn: async () => {
      try {
        const { data } = await api.get<ProjectionRequest>("/projecao/ultima-simulacao");
        return data;
      } catch (error) {
        if (axios.isAxiosError(error) && error.response?.status === 404) {
          return null;
        }
        throw error;
      }
    },
  });
}

export function useCalculateProjection() {
  return useMutation({
    mutationFn: async (payload: ProjectionRequest) => {
      const { data } = await api.post<ProjectionResult>("/projecao/calcular", payload);
      return data;
    },
  });
}
