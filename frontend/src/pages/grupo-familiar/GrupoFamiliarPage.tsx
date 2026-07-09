import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

import {
  useAcceptInvite,
  useChangeMemberRole,
  useDeclineInvite,
  useDeleteFamilyGroup,
  useInviteMember,
  useMyFamilyGroup,
  usePendingInvites,
  useRemoveMember,
} from "@/hooks/useFamilyGroup";
import { useAuth } from "@/context/AuthContext";
import { extractErrorMessage } from "@/lib/api";
import { FAMILY_ROLE_LABELS } from "@/types/familyGroup";
import type { FamilyGroupMemberResponseDTO } from "@/types/familyGroup";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Select";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { CreateFamilyGroupModal } from "@/pages/grupo-familiar/CreateFamilyGroupModal";

const inviteSchema = z.object({
  email: z.string().email("Informe um e-mail válido"),
  papel: z.enum(["MEMBER", "VIEWER"]),
});

type InviteFormValues = z.infer<typeof inviteSchema>;

function roleTone(papel: FamilyGroupMemberResponseDTO["papel"]) {
  if (papel === "OWNER") return "blue" as const;
  if (papel === "MEMBER") return "green" as const;
  return "slate" as const;
}

export function GrupoFamiliarPage() {
  const { user } = useAuth();
  const { data: group, isLoading, isError } = useMyFamilyGroup();
  const { data: invites, isLoading: isLoadingInvites } = usePendingInvites();

  const deleteGroup = useDeleteFamilyGroup();
  const inviteMember = useInviteMember();
  const acceptInvite = useAcceptInvite();
  const declineInvite = useDeclineInvite();
  const changeMemberRole = useChangeMemberRole();
  const removeMember = useRemoveMember();

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<InviteFormValues>({
    resolver: zodResolver(inviteSchema),
    defaultValues: { email: "", papel: "MEMBER" },
  });

  async function onInvite(values: InviteFormValues) {
    setActionError(null);
    try {
      await inviteMember.mutateAsync(values);
      reset({ email: "", papel: "MEMBER" });
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleAccept(id: number) {
    setActionError(null);
    try {
      await acceptInvite.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleDecline(id: number) {
    setActionError(null);
    try {
      await declineInvite.mutateAsync(id);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleChangeRole(userId: number, papel: "MEMBER" | "VIEWER") {
    setActionError(null);
    try {
      await changeMemberRole.mutateAsync({ userId, payload: { papel } });
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleRemoveMember(member: FamilyGroupMemberResponseDTO) {
    if (!window.confirm(`Remover "${member.nome}" do grupo?`)) return;
    setActionError(null);
    try {
      await removeMember.mutateAsync(member.userId);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleLeaveGroup() {
    const ownMember = group?.membros.find((m) => m.email === user?.email);
    if (!ownMember) return;
    if (!window.confirm("Sair do grupo familiar?")) return;
    setActionError(null);
    try {
      await removeMember.mutateAsync(ownMember.userId);
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  async function handleDeleteGroup() {
    if (!window.confirm("Excluir o grupo familiar? Essa ação não pode ser desfeita.")) return;
    setActionError(null);
    try {
      await deleteGroup.mutateAsync();
    } catch (error) {
      setActionError(extractErrorMessage(error));
    }
  }

  const isOwner = group?.papelDoUsuarioAtual === "OWNER";

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Grupo familiar</h1>
        <p className="text-sm text-slate-500">
          Compartilhe suas finanças com familiares, com controle de quem pode editar ou apenas visualizar.
        </p>
      </div>

      <ErrorBanner message={actionError} />

      {(isLoading || isLoadingInvites) && (
        <div className="flex justify-center p-6">
          <Spinner />
        </div>
      )}
      {isError && <ErrorBanner message="Não foi possível carregar o grupo familiar." />}

      {!isLoading && !group && (
        <>
          {!isLoadingInvites && invites && invites.length > 0 && (
            <div className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
              <h2 className="font-semibold text-slate-900">Convites pendentes</h2>
              <div className="flex flex-col gap-2">
                {invites.map((invite) => (
                  <div
                    key={invite.id}
                    className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2"
                  >
                    <div className="text-sm text-slate-700">
                      Convite para o grupo <strong>{invite.familyGroupNome}</strong> como{" "}
                      {FAMILY_ROLE_LABELS[invite.invitedRole]}
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="secondary"
                        onClick={() => handleAccept(invite.id)}
                        isLoading={acceptInvite.isPending}
                      >
                        Aceitar
                      </Button>
                      <Button
                        variant="danger"
                        onClick={() => handleDecline(invite.id)}
                        isLoading={declineInvite.isPending}
                      >
                        Recusar
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="flex flex-col items-start gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <h2 className="font-semibold text-slate-900">Você ainda não participa de um grupo familiar</h2>
            <p className="text-sm text-slate-500">
              Crie um grupo para compartilhar suas contas, receitas e despesas com outras pessoas.
            </p>
            <Button onClick={() => setShowCreateModal(true)}>Criar grupo</Button>
          </div>
        </>
      )}

      {!isLoading && group && (
        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <div>
              <h2 className="font-semibold text-slate-900">{group.nome}</h2>
              <p className="text-sm text-slate-500">
                Seu papel: <Badge tone={roleTone(group.papelDoUsuarioAtual)}>{FAMILY_ROLE_LABELS[group.papelDoUsuarioAtual]}</Badge>
              </p>
            </div>
            {isOwner ? (
              <Button variant="danger" onClick={handleDeleteGroup} isLoading={deleteGroup.isPending}>
                Excluir grupo
              </Button>
            ) : (
              <Button variant="danger" onClick={handleLeaveGroup} isLoading={removeMember.isPending}>
                Sair do grupo
              </Button>
            )}
          </div>

          <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Nome</th>
                  <th className="px-4 py-3 font-medium">E-mail</th>
                  <th className="px-4 py-3 font-medium">Papel</th>
                  {isOwner && <th className="px-4 py-3 font-medium text-right">Ações</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {group.membros.map((member) => (
                  <tr key={member.userId}>
                    <td className="px-4 py-3 font-medium text-slate-900">{member.nome}</td>
                    <td className="px-4 py-3 text-slate-600">{member.email}</td>
                    <td className="px-4 py-3">
                      {isOwner && member.userId !== group.ownerUserId ? (
                        <Select
                          value={member.papel === "OWNER" ? "MEMBER" : member.papel}
                          onChange={(e) => handleChangeRole(member.userId, e.target.value as "MEMBER" | "VIEWER")}
                        >
                          <option value="MEMBER">{FAMILY_ROLE_LABELS.MEMBER}</option>
                          <option value="VIEWER">{FAMILY_ROLE_LABELS.VIEWER}</option>
                        </Select>
                      ) : (
                        <Badge tone={roleTone(member.papel)}>{FAMILY_ROLE_LABELS[member.papel]}</Badge>
                      )}
                    </td>
                    {isOwner && (
                      <td className="px-4 py-3 text-right">
                        {member.userId !== group.ownerUserId && (
                          <Button
                            variant="danger"
                            onClick={() => handleRemoveMember(member)}
                            isLoading={removeMember.isPending}
                          >
                            Remover
                          </Button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {isOwner && (
            <form
              onSubmit={handleSubmit(onInvite)}
              className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-4 shadow-sm sm:flex-row sm:items-end"
            >
              <div className="flex-1">
                <Input
                  label="Convidar membro (e-mail)"
                  placeholder="nome@exemplo.com"
                  error={errors.email?.message}
                  {...register("email")}
                />
              </div>
              <div className="sm:w-48">
                <Select label="Papel" error={errors.papel?.message} {...register("papel")}>
                  <option value="MEMBER">{FAMILY_ROLE_LABELS.MEMBER}</option>
                  <option value="VIEWER">{FAMILY_ROLE_LABELS.VIEWER}</option>
                </Select>
              </div>
              <Button type="submit" isLoading={isSubmitting}>
                Convidar
              </Button>
            </form>
          )}
        </div>
      )}

      {showCreateModal && <CreateFamilyGroupModal onClose={() => setShowCreateModal(false)} />}
    </div>
  );
}
