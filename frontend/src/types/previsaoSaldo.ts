export interface PrevisaoSaldoPonto {
  anoMes: string;
  saldoProjetado: number;
}

export interface PrevisaoSaldo {
  saldoAtual: number;
  mediaMensalHistorica: number;
  serie: PrevisaoSaldoPonto[];
}
