export type GoalType = "MANUAL" | "INVESTIMENTO";

export const GOAL_TYPE_LABELS: Record<GoalType, string> = {
  MANUAL: "Manual",
  INVESTIMENTO: "Investimento",
};

export interface Goal {
  id: number;
  nome: string;
  valorAlvo: number;
  dataAlvo: string;
  valorAtual: number;
  ativa: boolean;
  progresso: number;
  tipo: GoalType;
  dataInicio: string;
}

export interface GoalRequest {
  nome: string;
  valorAlvo: number;
  dataAlvo: string;
  valorAtual?: number;
  ativa: boolean;
  tipo: GoalType;
  dataInicio?: string;
}

export interface UpdateGoalProgressRequest {
  valorAtual: number;
}
