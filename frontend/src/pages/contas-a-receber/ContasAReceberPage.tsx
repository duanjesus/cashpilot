import { ReceitasListPage } from "@/pages/receitas/ReceitasListPage";

export function ContasAReceberPage() {
  return (
    <ReceitasListPage
      initialFilters={{ recebida: false }}
      title="Contas a receber"
      description="Receitas pendentes de recebimento."
    />
  );
}
