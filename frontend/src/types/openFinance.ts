export type ContaOrigem = "MANUAL" | "OPEN_FINANCE";

export interface OpenFinanceInstitution {
  id: string;
  nome: string;
}

export type OpenFinanceTipoConta = "CONTA" | "CARTAO";

export interface ConnectInstitutionRequest {
  instituicaoNome: string;
  tipoConta: OpenFinanceTipoConta;
  apelido: string;
}
