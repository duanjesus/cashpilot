export interface Goal {
  id: number;
  nome: string;
  valorAlvo: number;
  dataAlvo: string;
  valorAtual: number;
  ativa: boolean;
  progresso: number;
}

export interface GoalRequest {
  nome: string;
  valorAlvo: number;
  dataAlvo: string;
  valorAtual: number;
  ativa: boolean;
}

export interface UpdateGoalProgressRequest {
  valorAtual: number;
}
