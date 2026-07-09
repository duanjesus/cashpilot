export interface Expense {
  id: number;
  descricao: string;
  valor: number;
  data: string;
  paga: boolean;
  dataPagamento: string | null;
  observacoes: string | null;
  categoriaId: number;
  categoriaNome: string;
  contaBancariaId: number | null;
  contaBancariaNome: string | null;
  cartaoCreditoId: number | null;
  cartaoCreditoNome: string | null;
}

export interface ExpenseRequest {
  descricao: string;
  valor: number;
  data: string;
  categoriaId: number;
  contaBancariaId?: number | null;
  cartaoCreditoId?: number | null;
  observacoes?: string;
}

export interface ExpenseFilters {
  page?: number;
  size?: number;
  dataInicio?: string;
  dataFim?: string;
  categoriaId?: number;
  contaId?: number;
  cartaoId?: number;
  paga?: boolean;
}

export interface MarkExpensePaidRequest {
  dataPagamento?: string | null;
}
