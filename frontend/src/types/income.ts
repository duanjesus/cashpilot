export interface Income {
  id: number;
  descricao: string;
  valor: number;
  data: string;
  recorrente: boolean;
  observacoes: string | null;
  categoriaId: number;
  categoriaNome: string;
  contaBancariaId: number;
  contaBancariaNome: string;
  recebida: boolean;
  dataRecebimento: string | null;
}

export interface IncomeRequest {
  descricao: string;
  valor: number;
  data: string;
  categoriaId: number;
  contaBancariaId: number;
  recorrente: boolean;
  observacoes?: string;
}

export interface IncomeFilters {
  page?: number;
  size?: number;
  dataInicio?: string;
  dataFim?: string;
  categoriaId?: number;
  contaId?: number;
  recebida?: boolean;
}

export interface MarkIncomeReceivedRequest {
  dataRecebimento?: string | null;
}
