export interface Transfer {
  id: number;
  valor: number;
  data: string;
  descricao: string | null;
  contaOrigemId: number;
  contaOrigemNome: string;
  contaDestinoId: number;
  contaDestinoNome: string;
}

export interface TransferRequest {
  contaOrigemId: number;
  contaDestinoId: number;
  valor: number;
  data: string;
  descricao?: string;
}

export interface TransferFilters {
  page?: number;
  size?: number;
}
