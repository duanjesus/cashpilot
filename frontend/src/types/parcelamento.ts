export interface Parcelamento {
  id: number;
  descricao: string;
  valorTotal: number;
  numeroParcelas: number;
  dataPrimeiraParcela: string;
  observacoes: string | null;
  categoriaId: number;
  categoriaNome: string;
  contaBancariaId: number | null;
  contaBancariaNome: string | null;
  cartaoCreditoId: number | null;
  cartaoCreditoNome: string | null;
  parcelasPagas: number;
  valorPago: number;
  valorRestante: number;
  quitado: boolean;
}

export interface ParcelamentoRequest {
  descricao: string;
  valorTotal: number;
  numeroParcelas: number;
  dataPrimeiraParcela: string;
  observacoes?: string;
  categoriaId: number;
  contaBancariaId?: number | null;
  cartaoCreditoId?: number | null;
}

export interface ParcelamentoFilters {
  page?: number;
  size?: number;
}
