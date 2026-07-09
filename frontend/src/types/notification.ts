export type NotificationTipo = "CONTA_A_VENCER" | "FATURA_FECHANDO" | "META_ATINGIDA";

export const NOTIFICATION_TIPO_LABELS: Record<NotificationTipo, string> = {
  CONTA_A_VENCER: "Conta a vencer",
  FATURA_FECHANDO: "Fatura fechando",
  META_ATINGIDA: "Meta atingida",
};

export interface NotificationResponseDTO {
  id: number;
  tipo: NotificationTipo;
  mensagem: string;
  referenciaTipo: string;
  referenciaId: number;
  lida: boolean;
  createdAt: string;
}
