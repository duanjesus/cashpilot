import { DespesasListPage } from "@/pages/despesas/DespesasListPage";

export function ContasAPagarPage() {
  return (
    <DespesasListPage
      initialFilters={{ paga: false }}
      initialSort="data,asc"
      title="Contas a pagar"
      description="Despesas pendentes, ordenadas pela data mais próxima."
    />
  );
}
