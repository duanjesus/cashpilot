export type TipoLancamentoFluxoCaixa = "DESPESA_PENDENTE" | "RECEITA_PENDENTE" | "ASSINATURA_PROJETADA";

export interface FluxoCaixaSeriePoint {
  data: string;
  saldoProjetado: number;
}

export interface FluxoCaixaDetalheItem {
  data: string;
  descricao: string;
  valor: number;
  tipo: TipoLancamentoFluxoCaixa;
  origemNome: string;
}

export interface FluxoCaixa {
  saldoInicial: number;
  serie: FluxoCaixaSeriePoint[];
  detalhamento: FluxoCaixaDetalheItem[];
}
