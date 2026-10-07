export type SaldoOrigem = "CAPTURADO" | "RECONSTRUIDO";

export interface SaldoHistoricoPonto {
  data: string;
  saldo: number;
  saldoRegistrado?: number;
  origem?: SaldoOrigem;
  divergente: boolean;
}
