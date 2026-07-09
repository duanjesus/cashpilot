export interface SimulacaoCenarioRequest {
  nome: string;
  patrimonioInicial: number;
  aporteMensal: number;
  taxaRetornoMensal: number;
}

export interface SimulacaoCompararRequest {
  horizonteMeses: number;
  cenarios: SimulacaoCenarioRequest[];
}

export interface SimulacaoCenarioResultado {
  nome: string;
  pontos: number[];
}

export interface SimulacaoCompararResponse {
  horizonteMeses: number;
  cenarios: SimulacaoCenarioResultado[];
}
