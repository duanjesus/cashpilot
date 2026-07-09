export type FamilyRole = "OWNER" | "MEMBER" | "VIEWER";

export const FAMILY_ROLE_LABELS: Record<FamilyRole, string> = {
  OWNER: "Dono",
  MEMBER: "Membro",
  VIEWER: "Visualizador",
};

export type InviteStatus = "PENDENTE" | "ACEITO" | "RECUSADO";

export const INVITE_STATUS_LABELS: Record<InviteStatus, string> = {
  PENDENTE: "Pendente",
  ACEITO: "Aceito",
  RECUSADO: "Recusado",
};

export interface FamilyGroupMemberResponseDTO {
  userId: number;
  nome: string;
  email: string;
  papel: FamilyRole;
}

export interface FamilyGroupResponseDTO {
  id: number;
  nome: string;
  ownerUserId: number;
  papelDoUsuarioAtual: FamilyRole;
  membros: FamilyGroupMemberResponseDTO[];
}

export interface FamilyInviteResponseDTO {
  id: number;
  familyGroupId: number;
  familyGroupNome: string;
  invitedEmail: string;
  invitedRole: FamilyRole;
  status: InviteStatus;
  createdAt: string;
}

export interface CreateFamilyGroupRequest {
  nome: string;
}

export interface InviteMemberRequest {
  email: string;
  papel: FamilyRole;
}

export interface ChangeMemberRoleRequest {
  papel: FamilyRole;
}
