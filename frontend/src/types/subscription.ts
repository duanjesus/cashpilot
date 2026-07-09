export interface Subscription {
  id: number;
  descricao: string;
  valor: number;
  diaCobranca: number;
  dataInicio: string;
  dataFim: string | null;
  ativa: boolean;
  observacoes: string | null;
  categoriaId: number;
  categoriaNome: string;
  contaBancariaId: number | null;
  contaBancariaNome: string | null;
  cartaoCreditoId: number | null;
  cartaoCreditoNome: string | null;
}

export interface SubscriptionRequest {
  descricao: string;
  valor: number;
  diaCobranca: number;
  dataInicio: string;
  dataFim?: string | null;
  ativa: boolean;
  observacoes?: string;
  categoriaId: number;
  contaBancariaId?: number | null;
  cartaoCreditoId?: number | null;
}
