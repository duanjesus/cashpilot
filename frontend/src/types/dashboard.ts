export interface UpcomingBill {
  id: number;
  descricao: string;
  valor: number;
  data: string;
  categoriaNome: string;
  diasRestantes: number;
}

export interface MainGoalSummary {
  id: number;
  nome: string;
  valorAlvo: number;
  valorAtual: number;
  progresso: number;
  dataAlvo: string;
}

export interface DashboardSummary {
  saldoAtual: number;
  entradasMes: number;
  saidasMes: number;
  investimentosMes: number;
  metaPrincipal: MainGoalSummary | null;
  proximasContas: UpcomingBill[];
}

export interface BalanceEvolutionPoint {
  data: string;
  valorAcumulado: number;
}
