import type { ContaOrigem } from "@/types/openFinance";

export type BankAccountType = "CORRENTE" | "POUPANCA" | "CARTEIRA" | "OUTRA";

export const BANK_ACCOUNT_TYPE_LABELS: Record<BankAccountType, string> = {
  CORRENTE: "Conta Corrente",
  POUPANCA: "Poupança",
  CARTEIRA: "Carteira",
  OUTRA: "Outra",
};

export interface BankAccount {
  id: number;
  nome: string;
  instituicao: string;
  tipo: BankAccountType;
  saldoInicial: number;
  dataSaldoInicial: string;
  ativa: boolean;
  saldoAtual: number;
  origem: ContaOrigem;
  instituicaoNome: string | null;
  ultimaSincronizacao: string | null;
}

export interface BankAccountRequest {
  nome: string;
  instituicao: string;
  tipo: BankAccountType;
  saldoInicial: number;
  dataSaldoInicial: string;
  ativa: boolean;
}
