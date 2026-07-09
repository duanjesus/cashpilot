export interface ProjectionRequest {
  salario: number;
  despesasFixas: number;
  despesasVariaveis: number;
  investimentoMensal?: number | null;
  patrimonioAtual: number;
  valorAlvo: number;
  taxaRetornoMensal: number;
}

export interface ProjectionResult {
  atingivel: boolean;
  aporteMensal: number;
  mesesParaAtingir: number | null;
  anos: number | null;
  mesesRestantes: number | null;
  dataEstimada: string | null;
  valorFinalProjetado: number;
  patrimonioAtual: number;
  valorAlvo: number;
  taxaRetornoMensal: number;
}
