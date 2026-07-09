import type { ContaOrigem } from "@/types/openFinance";

export type CardBrand = "VISA" | "MASTERCARD" | "ELO" | "AMEX" | "OUTRA";

export const CARD_BRAND_LABELS: Record<CardBrand, string> = {
  VISA: "Visa",
  MASTERCARD: "Mastercard",
  ELO: "Elo",
  AMEX: "Amex",
  OUTRA: "Outra",
};

export interface CreditCard {
  id: number;
  nome: string;
  bandeira: CardBrand;
  limite: number;
  diaFechamento: number;
  diaVencimento: number;
  contaVinculadaId: number | null;
  ativo: boolean;
  faturaAtual: number;
  origem: ContaOrigem;
  instituicaoNome: string | null;
  ultimaSincronizacao: string | null;
}

export interface CreditCardRequest {
  nome: string;
  bandeira: CardBrand;
  limite: number;
  diaFechamento: number;
  diaVencimento: number;
  contaVinculadaId?: number | null;
  ativo: boolean;
}
